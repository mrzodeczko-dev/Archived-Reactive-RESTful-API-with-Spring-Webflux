package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.*;
import com.rzodeczko.application.exception.StatisticsServiceException;
import com.rzodeczko.application.mapper.MovieMapper;
import com.rzodeczko.application.port.out.CityPort;
import com.rzodeczko.application.port.out.MoviePort;
import com.rzodeczko.application.port.out.TicketPurchasePort;
import com.rzodeczko.domain.cinema_hall.CinemaHall;
import com.rzodeczko.domain.city.City;
import com.rzodeczko.domain.movie.Movie;
import com.rzodeczko.domain.ticket.Ticket;
import com.rzodeczko.domain.ticket_purchase.TicketPurchase;
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

    private List<String> cinemaHallIdsForCity(City city) {
        return city.cinemas()
                .stream()
                .flatMap(cinema -> cinema.cinemaHalls().stream().map(CinemaHall::id))
                .collect(Collectors.toList());
    }

    public Flux<CityFrequencyDto> findCitiesFrequency() {
        final var currentDate = LocalDate.now();

        return cityPort.findAll()
                .flatMap(city -> ticketPurchasePort
                        .findAllByMovieEmissionInDateAndByCinemaHallsIdIn(
                                currentDate,
                                cinemaHallIdsForCity(city))
                        .map(ticketPurchase -> ticketPurchase.tickets().size())
                        .reduce(0, Integer::sum)
                        .map(totalTickets -> CityFrequencyDto.builder()
                                .city(city.name())
                                .frequency(totalTickets)
                                .build()));
    }

    public Flux<CityFrequencyDto> findCitiesWithMostFrequency() {
        return findCitiesFrequency()
                .collectList()
                .flatMapMany(list -> {
                    if (list.isEmpty()) {
                        return Flux.empty();
                    }
                    int max = Collections.max(list, Comparator.comparing(CityFrequencyDto::frequency))
                            .frequency();
                    return Flux.fromIterable(list)
                            .filter(dto -> dto.frequency().equals(max));
                });
    }

    public Flux<MovieFrequencyDto> findAllMoviesFrequency() {
        return moviePort.findAll()
                .flatMap(movie -> ticketPurchasePort.findAllByMovieId(movie.id())
                        .collectList()
                        .map(purchases -> MovieFrequencyDto.builder()
                                .movie(MovieMapper.toDto(movie))
                                .frequency(purchases.stream()
                                        .map(tp -> tp.tickets().size())
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
                                tp -> tp.movieEmission().movie().getGenre(),
                                tp -> tp.tickets().size())
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
        // Two parallel queries instead of N+1:
        //   T1 -> all cities  (used to build cinemaHallId -> cityName index)
        //   T2 -> all purchases (single scan, no per-city sub-queries)
        Mono<List<City>> citiesMono = cityPort.findAll().collectList();
        Mono<List<TicketPurchase>> purchasesMono = ticketPurchasePort.findAll().collectList();

        return Mono.zip(citiesMono, purchasesMono)
                .flatMapMany(tuple -> {
                    List<City> cities = tuple.getT1();
                    List<TicketPurchase> allPurchases = tuple.getT2();

                    // Build cinemaHallId -> cityName index in O(halls)
                    Map<String, String> hallToCity = new HashMap<>();
                    for (City city : cities) {
                        for (var cinema : city.cinemas()) {
                            for (var hall : cinema.cinemaHalls()) {
                                hallToCity.put(hall.id(), city.name());
                            }
                        }
                    }

                    // Single pass over all purchases: group by city, then by movie
                    Map<String, Map<Movie, Integer>> byCityByMovie = new LinkedHashMap<>();
                    for (TicketPurchase tp : allPurchases) {
                        String cityName = hallToCity.get(tp.movieEmission().cinemaHallId());
                        if (cityName == null) continue;
                        byCityByMovie
                                .computeIfAbsent(cityName, k -> new LinkedHashMap<>())
                                .merge(tp.movieEmission().movie(), tp.tickets().size(), Integer::sum);
                    }

                    // Emit one DTO per city, preserving the order returned by cityPort
                    return Flux.fromIterable(cities)
                            .map(city -> {
                                Map<Movie, Integer> movieFreqMap =
                                        byCityByMovie.getOrDefault(city.name(), Map.of());
                                int max = movieFreqMap.values().stream()
                                        .max(Integer::compareTo)
                                        .orElse(0);
                                List<MovieFrequencyDto> topMovies = movieFreqMap.entrySet().stream()
                                        .filter(e -> e.getValue() == max)
                                        .map(e -> MovieFrequencyDto.builder()
                                                .movie(MovieMapper.toDto(e.getKey()))
                                                .frequency(e.getValue())
                                                .build())
                                        .toList();
                                return MostPopularMovieGroupedByCityDto.builder()
                                        .city(city.name())
                                        .movieFrequency(topMovies)
                                        .build();
                            });
                });
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
                        .flatMap(tp -> Flux.fromIterable(tp.tickets()))
                        .collectList()
                        .map(tickets -> AverageTicketPriceByCityDto.builder()
                                .city(city.name())
                                .averageTicketPrice(averageTicketPrice(tickets))
                                .build()));
    }

    private BigDecimal averageTicketPrice(List<Ticket> tickets) {
        if (tickets.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return tickets.stream()
                .map(ticket -> ticket.price().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(tickets.size()), 2, RoundingMode.HALF_UP);
    }
}
