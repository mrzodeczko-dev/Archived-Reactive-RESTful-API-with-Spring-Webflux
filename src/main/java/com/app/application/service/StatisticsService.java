package com.app.application.service;

import com.app.application.dto.*;
import com.app.application.exception.StatisticsServiceException;
import com.app.domain.cinema_hall.CinemaHall;
import com.app.domain.city.CityRepository;
import com.app.domain.movie.MovieRepository;
import com.app.domain.ticket.Ticket;
import com.app.domain.ticket_purchase.TicketPurchase;
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

    private final TicketPurchaseRepository ticketPurchaseRepository;
    private final CityRepository cityRepository;
    private final MovieRepository movieRepository;

    private List<String> cinemaHallIdsForCity(com.app.domain.city.City city) {
        return city.getCinemas()
                .stream()
                .flatMap(cinema -> cinema.getCinemaHalls().stream().map(CinemaHall::getId))
                .collect(Collectors.toList());
    }

    public Flux<CityFrequencyDto> findCitiesFrequency() {
        final var currentDate = LocalDate.now();

        return cityRepository.findAll()
                .flatMap(city -> ticketPurchaseRepository
                        .findAllByMovieEmissionInDateAndByCinemaHallsIdIn(
                                currentDate,
                                cinemaHallIdsForCity(city))
                        .map(ticketPurchase -> ticketPurchase.getTickets().size())
                        .map(ticketsNumber -> CityFrequencyDto.builder()
                                .city(city.getName())
                                .frequency(ticketsNumber)
                                .build()));
    }

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
                .flatMapMany(city -> ticketPurchaseRepository
                        .findAllByCinemaHallsIds(cinemaHallIdsForCity(city))
                        .collectMultimap(
                                tp -> tp.getMovieEmission().getMovie().getGenre(),
                                tp -> tp.getTickets().size())
                        .map(this::reduceMultiMapToMapWithMaxElementOf)
                        .flatMapMany(maxByGenre -> Flux.fromIterable(
                                maxByGenre.entrySet().stream()
                                        .map(e -> MovieFrequencyByGenreDto.builder()
                                                .genre(e.getKey())
                                                .frequency(e.getValue())
                                                .build())
                                        .collect(Collectors.toList()))));
    }

    public Flux<MostPopularMovieGroupedByCityDto> findMostPopularMovieGroupedByCity() {
        return cityRepository.findAll()
                .flatMap(city -> ticketPurchaseRepository
                        .findAllByCinemaHallsIds(cinemaHallIdsForCity(city))
                        .collectMultimap(
                                tp -> tp.getMovieEmission().getMovie(),
                                tp -> tp.getTickets().size())
                        .map(this::reduceMultiMapToMapWithMaxElementOf)
                        .map(maxByMovie -> MostPopularMovieGroupedByCityDto.builder()
                                .city(city.getName())
                                .movieFrequency(maxByMovie.entrySet().stream()
                                        .map(e -> MovieFrequencyDto.builder()
                                                .movie(e.getKey().toDto())
                                                .frequency(e.getValue())
                                                .build())
                                        .toList())
                                .build()));
    }

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
                        .findAllByCinemaHallsIds(cinemaHallIdsForCity(city))
                        .flatMap(tp -> Flux.fromIterable(tp.getTickets()))
                        .collectList()
                        .map(tickets -> AverageTicketPriceByCityDto.builder()
                                .city(city.getName())
                                .averageTicketPrice(averageTicketPrice(tickets))
                                .build()));
    }

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
