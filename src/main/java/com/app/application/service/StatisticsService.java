package com.app.application.service;

import com.app.application.dto.*;
import com.app.application.exception.StatisticsServiceException;
import com.app.domain.cinema.CinemaRepository;
import com.app.domain.cinema_hall.CinemaHall;
import com.app.domain.cinema_hall.CinemaHallRepository;
import com.app.domain.city.CityRepository;
import com.app.domain.movie.MovieRepository;
import com.app.domain.movie_emission.MovieEmissionRepository;
import com.app.domain.ticket.Ticket;
import com.app.domain.ticket_order.TicketOrderRepository;
import com.app.domain.ticket_purchase.TicketPurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final CinemaHallRepository cinemaHallRepository;
    private final TicketOrderRepository ticketOrderRepository;
    private final TicketPurchaseRepository ticketPurchaseRepository;
    private final CinemaRepository cinemaRepository;
    private final CityRepository cityRepository;
    private final MovieRepository movieRepository;
    private final MovieEmissionRepository movieEmissionRepository;

    public Flux<CityFrequencyDto> findCitiesFrequency() {
        final var currentDate = LocalDate.now();

        return cityRepository.findAll()
                .flatMap(city -> ticketPurchaseRepository.findAllByMovieEmissionInDateAndByCinemaHallsIdIn(
                                currentDate,
                                city.getCinemas()
                                        .stream()
                                        .flatMap(cinema -> cinema.getCinemaHalls().stream().map(CinemaHall::getId))
                                        .collect(Collectors.toList()))
                        .map(ticketPurchase -> ticketPurchase.getTickets().size())
                        .map(ticketsNumber -> CityFrequencyDto.builder()
                                .city(city.getName())
                                .frequency(ticketsNumber)
                                .build()));
    }

    /**
     * Fixed double subscription on cold Flux.
     * Previously findCitiesFrequency() was subscribed twice (once for collectList to find max,
     * once for filter), causing two separate DB query rounds with potentially inconsistent results.
     * Now: collect once into a list, then find max and filter in-memory.
     */
    public Flux<CityFrequencyDto> findCitiesWithMostFrequency() {
        return findCitiesFrequency()
                .collectList()
                .flatMapMany(list -> {
                    if (list.isEmpty()) {
                        return Flux.empty();
                    }
                    int max = Collections.max(list, Comparator.comparing(CityFrequencyDto::getFrequency))
                            .getFrequency();
                    return Flux.fromIterable(list)
                            .filter(dto -> dto.getFrequency().equals(max));
                });
    }

    public Flux<MovieFrequencyDto> findAllMoviesFrequency() {
        return movieRepository.findAll()
                .flatMap(movie -> ticketPurchaseRepository.findAllByMovieId(movie.getId())
                        .collectList()
                        .map(purchases -> MovieFrequencyDto.builder()
                                .movie(movie.toDto())
                                .frequency(purchases.stream()
                                        .map(tp -> tp.getTickets().size())
                                        .reduce(0, Integer::sum))
                                .build()));
    }

    public Flux<MovieFrequencyByGenreDto> findMostPopularMoviesGroupedByGenreInCity(String cityName) {
        if (Objects.isNull(cityName)) {
            return Flux.error(() -> new StatisticsServiceException("City name is required"));
        }

        return cityRepository.findByName(cityName)
                .switchIfEmpty(Mono.error(() -> new StatisticsServiceException(
                        "No city with name: %s".formatted(cityName))))
                .flatMap(city -> cinemaRepository.findAllByCity(city.getName())
                        .flatMap(cinema -> cinemaHallRepository.getAllForCinemaById(cinema.getId()))
                        .flatMap(cinemaHall -> movieEmissionRepository.findMovieEmissionsByCinemaHallId(cinemaHall.getId()))
                        .flatMap(movieEmission -> ticketPurchaseRepository
                                .findAllByMovieId(movieEmission.getMovie().getId())
                                .collectMultimap(
                                        tp -> tp.getMovieEmission().getMovie().getGenre(),
                                        tp -> tp.getTickets().size())
                                .map(this::reduceMultiMapToMapWithMaxElementOf))
                        .reduce(new ArrayList<MovieFrequencyByGenreDto>(), (list, subMap) -> {
                            list.addAll(subMap.entrySet().stream()
                                    .map(e -> MovieFrequencyByGenreDto.builder()
                                            .genre(e.getKey())
                                            .frequency(e.getValue())
                                            .build())
                                    .collect(Collectors.toList()));
                            return list;
                        }))
                .flatMapMany(Flux::fromIterable);
    }

    public Flux<MostPopularMovieGroupedByCityDto> findMostPopularMovieGroupedByCity() {
        return cityRepository.findAll()
                .flatMap(city -> cinemaRepository.findAllByCity(city.getName())
                        .flatMap(cinema -> cinemaHallRepository.getAllForCinemaById(cinema.getId()))
                        .flatMap(cinemaHall -> movieEmissionRepository.findMovieEmissionsByCinemaHallId(cinemaHall.getId()))
                        .flatMap(movieEmission -> ticketPurchaseRepository
                                .findAllByMovieId(movieEmission.getMovie().getId())
                                .collectMultimap(
                                        tp -> tp.getMovieEmission().getMovie(),
                                        tp -> tp.getTickets().size())
                                .map(this::reduceMultiMapToMapWithMaxElementOf))
                        .map(movieFrequencyMap -> MostPopularMovieGroupedByCityDto.builder()
                                .city(city.getName())
                                .movieFrequency(movieFrequencyMap.entrySet().stream()
                                        .map(e -> MovieFrequencyDto.builder()
                                                .movie(e.getKey().toDto())
                                                .frequency(e.getValue())
                                                .build())
                                        .toList())
                                .build()));
    }

    /**
     * Fixed two bugs:
     * 1. numberOfSameMaxVal was never incremented, so limit(0) always produced an empty map.
     *    Removed the broken limit() — all entries are now returned after summing by key.
     * 2. Logic simplified: collect to map summing values, then filter only entries matching the max.
     */
    private <T> Map<T, Integer> reduceMultiMapToMapWithMaxElementOf(Map<T, Collection<Integer>> multiMap) {
        Map<T, Integer> summedMap = multiMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().reduce(0, Integer::sum),
                        Integer::sum,
                        LinkedHashMap::new));

        int max = summedMap.values().stream().max(Integer::compareTo).orElse(0);

        return summedMap.entrySet().stream()
                .filter(e -> e.getValue() == max)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Integer::sum,
                        LinkedHashMap::new));
    }

    public Flux<AverageTicketPriceByCityDto> getAverageTicketPriceGroupedByCity() {
        return cityRepository.findAll()
                .flatMap(city -> ticketPurchaseRepository
                        .findAllByCinemaHallsIds(city.getCinemas().stream()
                                .flatMap(cinema -> cinema.getCinemaHalls().stream().map(CinemaHall::getId))
                                .collect(Collectors.toList()))
                        .collectMap(
                                ticketPurchase -> city,
                                ticketPurchase -> averageTicketPrice(ticketPurchase.getTickets())))
                .map(entry -> AverageTicketPriceByCityDto.builder()
                        .city(entry.keySet().iterator().next().getName())
                        .averageTicketPrice(entry.values().iterator().next())
                        .build());
    }

    /**
     * Fixed potential ArithmeticException (/ by zero).
     * Previously counter.get() was evaluated as argument to divide() before the stream terminal
     * operation ran, so it was always 0. Replaced with tickets.size().
     */
    private BigDecimal averageTicketPrice(List<Ticket> tickets) {
        if (tickets.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return tickets.stream()
                .map(ticket -> ticket.getPrice().getValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(tickets.size()), 2, RoundingMode.HALF_UP);
    }
}
