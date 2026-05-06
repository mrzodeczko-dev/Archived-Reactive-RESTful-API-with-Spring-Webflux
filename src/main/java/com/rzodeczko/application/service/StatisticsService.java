package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.*;
import com.rzodeczko.application.exception.StatisticsServiceException;
import com.rzodeczko.application.mapper.MovieMapper;
import com.rzodeczko.application.port.out.CityPort;
import com.rzodeczko.application.port.out.MoviePort;
import com.rzodeczko.application.port.out.TicketPurchasePort;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.ticket.Ticket;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsService {

    private final TicketPurchasePort ticketPurchasePort;
    private final CityPort cityPort;
    private final MoviePort moviePort;

    public StatisticsService(TicketPurchasePort ticketPurchasePort, CityPort cityPort, MoviePort moviePort) {
        this.ticketPurchasePort = ticketPurchasePort;
        this.cityPort = cityPort;
        this.moviePort = moviePort;
    }

    private List<String> cinemaHallIdsForCity(com.rzodeczko.domain.city.City city) {
        return city.getCinemas()
                .stream()
                .flatMap(cinema -> cinema.getCinemaHalls().stream().map(CinemaHall::getId))
                .collect(Collectors.toList());
    }

    public Flux<CityFrequencyDto> findCitiesFrequency() {
        final var currentDate = LocalDate.now();

        return cityPort.findAll()
                .flatMap(city -> ticketPurchasePort
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
        return moviePort.findAll()
                .flatMap(movie -> ticketPurchasePort.findAllByMovieId(movie.getId())
                        .collectList()
                        .map(purchases -> MovieFrequencyDto.builder()
                                .movie(MovieMapper.toDto(movie))
                                .frequency(purchases.stream()
                                        .map(tp -> tp.getTickets().size())
                                        .reduce(0, Integer::sum))
                                .build()));
    }

    public Flux<MovieFrequencyByGenreDto> findMostPopularMoviesGroupedByGenreInCity(String cityName) {
        if (Objects.isNull(cityName)) {
            return Flux.error(() -> new StatisticsServiceException("City name is required"));
        }

        return cityPort.findByName(cityName)
                .switchIfEmpty(Mono.error(() -> new StatisticsServiceException(
                        "No city with name: %s".formatted(cityName))))
                .flatMapMany(city -> ticketPurchasePort
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
        return cityPort.findAll()
                .flatMap(city -> ticketPurchasePort
                        .findAllByCinemaHallsIds(cinemaHallIdsForCity(city))
                        .collectMultimap(
                                tp -> tp.getMovieEmission().getMovie(),
                                tp -> tp.getTickets().size())
                        .map(this::reduceMultiMapToMapWithMaxElementOf)
                        .map(maxByMovie -> MostPopularMovieGroupedByCityDto.builder()
                                .city(city.getName())
                                .movieFrequency(maxByMovie.entrySet().stream()
                                        .map(e -> MovieFrequencyDto.builder()
                                                .movie(MovieMapper.toDto(e.getKey()))
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
        return cityPort.findAll()
                .flatMap(city -> ticketPurchasePort
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
