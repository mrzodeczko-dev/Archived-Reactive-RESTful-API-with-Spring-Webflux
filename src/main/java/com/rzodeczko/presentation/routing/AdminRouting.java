package com.rzodeczko.presentation.routing;

import com.rzodeczko.presentation.routing.handlers.*;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * All administration endpoints, consolidated under the {@code /admin} prefix.
 *
 * <p>Access is enforced globally by {@code WebSecurityConfig}:
 * every path matching {@code /admin/**} requires the {@code ROLE_ADMIN} authority.
 */
@Configuration
public class AdminRouting extends BaseJsonRouter {

    @Bean
    @RouterOperations({
            // Cinema Halls
            @RouterOperation(method = RequestMethod.POST,
                    path = "/admin/cinemaHalls/addToCinema/cinemaId/{cinemaId}",
                    beanClass = CinemaHallsHandler.class, beanMethod = "addCinemaHallToCinema"),
            @RouterOperation(method = RequestMethod.POST,
                    path = "/admin/cinemaHalls/cinemaId/{cinemaId}/csv",
                    beanClass = CinemaHallsHandler.class, beanMethod = "addCinemaHallsWithCsvFile"),

            // Cinemas
            @RouterOperation(method = RequestMethod.POST,
                    path = "/admin/cinemas",
                    beanClass = CinemasHandler.class, beanMethod = "addCinema"),
            @RouterOperation(method = RequestMethod.POST,
                    path = "/admin/cinemas/csv",
                    beanClass = CinemasHandler.class, beanMethod = "addCinemasWithCsvFile"),
            @RouterOperation(method = RequestMethod.PUT,
                    path = "/admin/cinemas/id/{id}/addCinemaHall",
                    beanClass = CinemasHandler.class, beanMethod = "addCinemaHall"),

            //  Cities
            @RouterOperation(method = RequestMethod.POST,
                    path = "/admin/cities",
                    beanClass = CitiesHandler.class, beanMethod = "addCity"),
            @RouterOperation(method = RequestMethod.POST,
                    path = "/admin/cities/csv",
                    beanClass = CitiesHandler.class, beanMethod = "addCitiesWithCsvFile"),
            @RouterOperation(method = RequestMethod.PUT,
                    path = "/admin/cities",
                    beanClass = CitiesHandler.class, beanMethod = "addCinemaToCity"),

            //  Movies
            @RouterOperation(method = RequestMethod.POST,
                    path = "/admin/movies",
                    beanClass = MoviesHandler.class, beanMethod = "addMovieToDatabase"),
            @RouterOperation(method = RequestMethod.POST,
                    path = "/admin/movies/csv",
                    beanClass = MoviesHandler.class, beanMethod = "addMovieToDatabaseWithCsvFile"),
            @RouterOperation(method = RequestMethod.DELETE,
                    path = "/admin/movies/id/{id}",
                    beanClass = MoviesHandler.class, beanMethod = "deleteMovieById"),
            @RouterOperation(method = RequestMethod.DELETE,
                    path = "/admin/movies",
                    beanClass = MoviesHandler.class, beanMethod = "deleteAllMovies"),

            // Movie Emissions
            @RouterOperation(method = RequestMethod.POST,
                    path = "/admin/movieEmissions",
                    beanClass = MovieEmissionsHandler.class, beanMethod = "addMovieEmission"),
            @RouterOperation(method = RequestMethod.POST,
                    path = "/admin/movieEmissions/csv",
                    beanClass = MovieEmissionsHandler.class, beanMethod = "addMovieEmissionsWithCsvFile"),
            @RouterOperation(method = RequestMethod.DELETE,
                    path = "/admin/movieEmissions/{id}",
                    beanClass = MovieEmissionsHandler.class, beanMethod = "deleteMovieEmissionById"),

            // Statistics
            @RouterOperation(method = RequestMethod.GET,
                    path = "/admin/statistics/cities/cinemaFrequency",
                    beanClass = StatisticsHandler.class, beanMethod = "getCinemaFrequencyByCityForAllCities"),
            @RouterOperation(method = RequestMethod.GET,
                    path = "/admin/statistics/cities/cinemaFrequency/max",
                    beanClass = StatisticsHandler.class, beanMethod = "getCityWithMaxFrequency"),
            @RouterOperation(method = RequestMethod.GET,
                    path = "/admin/statistics/movies/mostPopular/byCity",
                    beanClass = StatisticsHandler.class, beanMethod = "findMostPopularMovieGroupedByCity"),
            @RouterOperation(method = RequestMethod.GET,
                    path = "/admin/statistics/movies/frequency",
                    beanClass = StatisticsHandler.class, beanMethod = "findAllMoviesFrequency"),
            @RouterOperation(method = RequestMethod.GET,
                    path = "/admin/statistics/movies/mostPopularGroupedByGenre/byCity/{city}",
                    beanClass = StatisticsHandler.class, beanMethod = "findMostPopularMoviesGroupedByGenreInCity"),
            @RouterOperation(method = RequestMethod.GET,
                    path = "/admin/statistics/averageTicketPrice",
                    beanClass = StatisticsHandler.class, beanMethod = "getAverageTicketPriceGroupedByCity"),

            // Users
            @RouterOperation(method = RequestMethod.GET,
                    path = "/admin/users",
                    beanClass = UsersHandler.class, beanMethod = "getAllUsers"),
            @RouterOperation(method = RequestMethod.DELETE,
                    path = "/admin/users",
                    beanClass = UsersHandler.class, beanMethod = "deleteAllUsers"),
            @RouterOperation(method = RequestMethod.GET,
                    path = "/admin/users/username/{username}",
                    beanClass = UsersHandler.class, beanMethod = "getByUsername"),
            @RouterOperation(method = RequestMethod.DELETE,
                    path = "/admin/users/username/{username}",
                    beanClass = UsersHandler.class, beanMethod = "deleteByUsername"),
            @RouterOperation(method = RequestMethod.POST,
                    path = "/admin/users/promoteToAdmin/username/{username}",
                    beanClass = UsersHandler.class, beanMethod = "promoteUserToAdminRole"),
            @RouterOperation(method = RequestMethod.POST,
                    path = "/admin/emails/send/multiple",
                    beanClass = EmailHandler.class, beanMethod = "sendMultipleEmails")
    })
    public RouterFunction<ServerResponse> adminRouterFunction(
            CinemaHallsHandler cinemaHallsHandler,
            CinemasHandler cinemasHandler,
            CitiesHandler citiesHandler,
            MoviesHandler moviesHandler,
            MovieEmissionsHandler movieEmissionsHandler,
            StatisticsHandler statisticsHandler,
            UsersHandler usersHandler,
            EmailHandler emailHandler
    ) {

        return route()
                .path("/admin", admin -> admin

                        //  Cinema Halls
                        .path("/cinemaHalls", builder -> builder
                                .nest(jsonAccept(), nested -> nested
                                        .POST("/addToCinema/cinemaId/{cinemaId}", cinemaHallsHandler::addCinemaHallToCinema)
                                        .POST("/cinemaId/{cinemaId}/csv", cinemaHallsHandler::addCinemaHallsWithCsvFile)
                                )
                        )

                        // Cinemas
                        .path("/cinemas", builder -> builder
                                .nest(jsonAccept(), nested -> nested
                                        .POST("", cinemasHandler::addCinema)
                                        .POST("/csv", cinemasHandler::addCinemasWithCsvFile)
                                        .PUT("/id/{id}/addCinemaHall", cinemasHandler::addCinemaHall)
                                )
                        )

                        // Cities
                        .path("/cities", builder -> builder
                                .nest(jsonAccept(), nested -> nested
                                        .POST("", citiesHandler::addCity)
                                        .POST("/csv", citiesHandler::addCitiesWithCsvFile)
                                        .PUT("", citiesHandler::addCinemaToCity)
                                )
                        )

                        //  Movies
                        .path("/movies", builder -> builder
                                .nest(jsonAccept(), nested -> nested
                                        .POST("", moviesHandler::addMovieToDatabase)
                                        .DELETE("/id/{id}", moviesHandler::deleteMovieById)
                                        .DELETE("", _ -> moviesHandler.deleteAllMovies())
                                )
                                .POST("/csv", jsonAccept(), moviesHandler::addMovieToDatabaseWithCsvFile)
                        )

                        // Movie Emissions
                        .path("/movieEmissions", builder -> builder
                                .nest(jsonAccept(), nested -> nested
                                        .POST("", movieEmissionsHandler::addMovieEmission)
                                        .POST("/csv", movieEmissionsHandler::addMovieEmissionsWithCsvFile)
                                        .DELETE("/{id}", movieEmissionsHandler::deleteMovieEmissionById)
                                )
                        )

                        // Statistics
                        .path("/statistics", builder -> builder
                                .nest(jsonAccept(), nested -> nested
                                        .GET("/cities/cinemaFrequency", _ -> statisticsHandler.getCinemaFrequencyByCityForAllCities())
                                        .GET("/cities/cinemaFrequency/max", _ -> statisticsHandler.getCityWithMaxFrequency())
                                        .GET("/movies/mostPopular/byCity", _ -> statisticsHandler.findMostPopularMovieGroupedByCity())
                                        .GET("/movies/frequency", _ -> statisticsHandler.findAllMoviesFrequency())
                                        .GET("/movies/mostPopularGroupedByGenre/byCity/{city}", statisticsHandler::findMostPopularMoviesGroupedByGenreInCity)
                                        .GET("/averageTicketPrice", _ -> statisticsHandler.getAverageTicketPriceGroupedByCity())
                                )
                        )
                        // Users
                        .path("/users", builder -> builder
                                .nest(jsonAccept(), nested -> nested
                                        .GET("", _ -> usersHandler.getAllUsers())
                                        .DELETE("", _ -> usersHandler.deleteAllUsers())
                                        .GET("/username/{username}", usersHandler::getByUsername)
                                        .DELETE("/username/{username}", usersHandler::deleteByUsername)
                                        .POST("/promoteToAdmin/username/{username}", usersHandler::promoteUserToAdminRole)
                                )
                        )
                        //Email
                        .path("/emails", builder -> builder
                                .nest(jsonAccept(), nested -> nested
                                        .POST("/send/multiple", emailHandler::sendMultipleEmails)
                                )
                        ))
                .build();
    }
}
