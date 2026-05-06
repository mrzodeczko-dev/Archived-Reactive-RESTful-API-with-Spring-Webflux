package com.rzodeczko.presentation.routing;

import com.rzodeczko.presentation.routing.handlers.*;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AppRouting {

    @Bean
    @RouterOperations({
            @RouterOperation(path = "/register", beanClass = UsersHandler.class, beanMethod = "register"),
            @RouterOperation(path = "/users", beanClass = UsersHandler.class, beanMethod = "getAllUsers"),
            @RouterOperation(path = "/users/username/{username}", beanClass = UsersHandler.class, beanMethod = "getByUsername"),
            @RouterOperation(path = "/users/promoteToAdmin/username/{username}", beanClass = UsersHandler.class, beanMethod = "promoteUserToAdminRole")
    })
    public RouterFunction<ServerResponse> usersRoute(UsersHandler usersHandler) {
        return route(POST("/register").and(accept(MediaType.APPLICATION_JSON)), usersHandler::register)
                .andRoute(GET("/users").and(accept(MediaType.APPLICATION_JSON)), usersHandler::getAllUsers)
                .andRoute(GET("/users/username/{username}").and(accept(MediaType.APPLICATION_JSON)), usersHandler::getByUsername)
                .andRoute(POST("/users/promoteToAdmin/username/{username}").and(accept(MediaType.APPLICATION_JSON)), usersHandler::promoteUserToAdminRole);
    }

    @Bean
    @RouterOperations({
            @RouterOperation(path = "/login", beanClass = LoginHandler.class, beanMethod = "login")
    })
    public RouterFunction<ServerResponse> loginRoute(LoginHandler loginHandler) {
        return route(POST("/login").and(accept(MediaType.APPLICATION_JSON)), loginHandler::login);
    }

    @Bean
    @RouterOperations({
            @RouterOperation(path = "/emails/send/single", beanClass = EmailHandler.class, beanMethod = "sendSingleEmail"),
            @RouterOperation(path = "/emails/send/multiple", beanClass = EmailHandler.class, beanMethod = "sendMultipleEmails")
    })
    public RouterFunction<ServerResponse> emailsRoute(EmailHandler emailHandler) {
        return route(POST("/emails/send/single").and(accept(MediaType.APPLICATION_JSON)), emailHandler::sendSingleEmail)
                .andRoute(POST("/emails/send/multiple").and(accept(MediaType.APPLICATION_JSON)), emailHandler::sendMultipleEmails);
    }

    @Bean
    @RouterOperations({
            @RouterOperation(path = "/cinemaHalls", beanClass = CinemaHallsHandler.class, beanMethod = "getAll"),
            @RouterOperation(path = "/cinemaHalls/cinemaId/{cinemaId}", beanClass = CinemaHallsHandler.class, beanMethod = "getAllForCinema"),
            @RouterOperation(path = "/cinemaHalls/addToCinema/cinemaId/{cinemaId}", beanClass = CinemaHallsHandler.class, beanMethod = "addCinemaHallToCinema")
    })
    public RouterFunction<ServerResponse> cinemaHallsRoute(CinemaHallsHandler cinemaHallsHandler) {
        return route(POST("/cinemaHalls/addToCinema/cinemaId/{cinemaId}").and(accept(MediaType.APPLICATION_JSON)), cinemaHallsHandler::addCinemaHallToCinema)
                .andRoute(GET("/cinemaHalls/cinemaId/{cinemaId}").and(accept(MediaType.APPLICATION_JSON)), cinemaHallsHandler::getAllForCinema)
                .andRoute(GET("/cinemaHalls").and(accept(MediaType.APPLICATION_JSON)), cinemaHallsHandler::getAll);
    }

    @Bean
    @RouterOperations({
            @RouterOperation(path = "/movies/id/{id}", beanClass = MoviesHandler.class, beanMethod = "getById"),
            @RouterOperation(path = "/movies/addToFavorites/{id}", beanClass = MoviesHandler.class, beanMethod = "addMovieToFavorites"),
            @RouterOperation(path = "/movies", beanClass = MoviesHandler.class, beanMethod = "addMovieToDatabase"),
            @RouterOperation(path = "/movies/id/{id}", beanClass = MoviesHandler.class, beanMethod = "deleteMovieById"),
            @RouterOperation(path = "/movies", beanClass = MoviesHandler.class, beanMethod = "getAllMovies"),
            @RouterOperation(path = "/movies/filter/premiereDate", beanClass = MoviesHandler.class, beanMethod = "getMoviesFilteredByPremiereDate"),
            @RouterOperation(path = "/movies/filter/duration", beanClass = MoviesHandler.class, beanMethod = "getMoviesFilteredByDuration"),
            @RouterOperation(path = "/movies/filter/name/{name}", beanClass = MoviesHandler.class, beanMethod = "getMoviesFilteredByName"),
            @RouterOperation(path = "/movies/filter/genre/{genre}", beanClass = MoviesHandler.class, beanMethod = "getMoviesFilteredByGenre"),
            @RouterOperation(path = "/movies/filter/keyword/{keyword}", beanClass = MoviesHandler.class, beanMethod = "getMoviesFilteredByKeyword"),
            @RouterOperation(path = "/movies/csv", beanClass = MoviesHandler.class, beanMethod = "addMovieToDatabaseWithCsvFile"),
            @RouterOperation(path = "/movies/favorites", beanClass = MoviesHandler.class, beanMethod = "getFavoriteMovies")
    })
    public RouterFunction<ServerResponse> moviesRoute(MoviesHandler moviesHandler) {
        return route(GET("/movies/id/{id}").and(accept(MediaType.APPLICATION_JSON)), moviesHandler::getById)
                .andRoute(PATCH("/movies/addToFavorites/{id}").and(accept(MediaType.APPLICATION_JSON)), moviesHandler::addMovieToFavorites)
                .andRoute(POST("/movies").and(accept(MediaType.APPLICATION_JSON)), moviesHandler::addMovieToDatabase)
                .andRoute(DELETE("/movies/id/{id}").and(accept(MediaType.APPLICATION_JSON)), moviesHandler::deleteMovieById)
                .andRoute(GET("/movies").and(accept(MediaType.APPLICATION_JSON)), moviesHandler::getAllMovies)
                .andRoute(GET("/movies/filter/premiereDate").and(accept(MediaType.APPLICATION_JSON)), moviesHandler::getMoviesFilteredByPremiereDate)
                .andRoute(GET("/movies/filter/duration").and(accept(MediaType.APPLICATION_JSON)), moviesHandler::getMoviesFilteredByDuration)
                .andRoute(GET("/movies/filter/name/{name}").and(accept(MediaType.APPLICATION_JSON)), moviesHandler::getMoviesFilteredByName)
                .andRoute(GET("/movies/filter/genre/{genre}").and(accept(MediaType.APPLICATION_JSON)), moviesHandler::getMoviesFilteredByGenre)
                .andRoute(GET("/movies/filter/keyword/{keyword}").and(accept(MediaType.APPLICATION_JSON)), moviesHandler::getMoviesFilteredByKeyword)
                .andRoute(POST("/movies/csv").and(accept(MediaType.APPLICATION_JSON)), moviesHandler::addMovieToDatabaseWithCsvFile)
                .andRoute(GET("/movies/favorites").and(accept(MediaType.APPLICATION_JSON)), moviesHandler::getFavoriteMovies);
    }

    @RouterOperations({
            @RouterOperation(path = "/ticketOrders", beanClass = TicketOrderHandler.class, beanMethod = "orderTickets"),
            @RouterOperation(path = "/ticketsOrders/cancel/orderId/{orderId}", beanClass = TicketOrderHandler.class, beanMethod = "cancelOrder"),
            @RouterOperation(path = "/ticketsOrders/username", beanClass = TicketOrderHandler.class, beanMethod = "getAllTicketOrdersByUsername")
    })
    @Bean
    public RouterFunction<ServerResponse> tickerOrdersRouting(TicketOrderHandler ticketOrderHandler) {
        return route(POST("/ticketOrders").and(accept(MediaType.APPLICATION_JSON)), ticketOrderHandler::orderTickets)
                .andRoute(PUT("/ticketsOrders/cancel/orderId/{orderId}").and(accept(MediaType.APPLICATION_JSON)), ticketOrderHandler::cancelOrder)
                .andRoute(GET("/ticketsOrders/username").and(accept(MediaType.APPLICATION_JSON)), ticketOrderHandler::getAllTicketOrdersByUsername);
    }

    @RouterOperations({
            @RouterOperation(method = RequestMethod.POST, path = "/cinemas", beanClass = CinemasHandler.class, beanMethod = "addCinema"),
            @RouterOperation(method = RequestMethod.GET, path = "/cinemas", beanClass = CinemasHandler.class, beanMethod = "getAll"),
            @RouterOperation(method = RequestMethod.GET, path = "/cinemas/city/{cityId}", beanClass = CinemasHandler.class, beanMethod = "getAllCinemasByCity"),
            @RouterOperation(method = RequestMethod.PUT, path = "/cinemas/id/{id}/addCinemaHall", beanClass = CinemasHandler.class, beanMethod = "addCinemaHall")
    })
    @Bean
    public RouterFunction<ServerResponse> cinemasRouting(CinemasHandler cinemasHandler) {
        return route(POST("/cinemas").and(accept(MediaType.APPLICATION_JSON)), cinemasHandler::addCinema)
                .andRoute(GET("/cinemas").and(accept(MediaType.APPLICATION_JSON)), cinemasHandler::getAll)
                .andRoute(GET("/cinemas/city/{city}").and(accept(MediaType.APPLICATION_JSON)), cinemasHandler::getAllCinemasByCity)
                .andRoute(PUT("/cinemas/id/{id}/addCinemaHall").and(accept(MediaType.APPLICATION_JSON)), cinemasHandler::addCinemaHall);
    }

    @RouterOperations({
            @RouterOperation(method = RequestMethod.POST, path = "/movieEmissions", beanClass = MovieEmissionsHandler.class, beanMethod = "addMovieEmission"),
            @RouterOperation(method = RequestMethod.GET, path = "/movieEmissions", beanClass = MovieEmissionsHandler.class, beanMethod = "getAllMovieEmissions"),
            @RouterOperation(method = RequestMethod.GET, path = "/movieEmissions/movieId/{movieId}", beanClass = MovieEmissionsHandler.class, beanMethod = "getAllMovieEmissionsByMovieId"),
            @RouterOperation(method = RequestMethod.GET, path = "/movieEmissions/cinemaHallId/{cinemaHallId}", beanClass = MovieEmissionsHandler.class, beanMethod = "getAllMovieEmissionsByCinemaHallId"),
            @RouterOperation(method = RequestMethod.DELETE, path = "/movieEmissions/{id}", beanClass = MovieEmissionsHandler.class, beanMethod = "deleteMovieEmissionById")
    })
    @Bean
    public RouterFunction<ServerResponse> movieEmissionsRouting(MovieEmissionsHandler movieEmissionsHandler) {
        return route(POST("/movieEmissions").and(accept(MediaType.APPLICATION_JSON)), movieEmissionsHandler::addMovieEmission)
                .andRoute(GET("/movieEmissions/movieId/{movieId}").and(accept(MediaType.APPLICATION_JSON)), movieEmissionsHandler::getAllMovieEmissionsByMovieId)
                .andRoute(GET("/movieEmissions").and(accept(MediaType.APPLICATION_JSON)), movieEmissionsHandler::getAllMovieEmissions)
                .andRoute(GET("/movieEmissions/cinemaHallId/{cinemaHallId}").and(accept(MediaType.APPLICATION_JSON)), movieEmissionsHandler::getAllMovieEmissionsByCinemaHallId)
                .andRoute(DELETE("/movieEmissions/{id}").and(accept(MediaType.APPLICATION_JSON)), movieEmissionsHandler::deleteMovieEmissionById);
    }

    @RouterOperations({
            @RouterOperation(method = RequestMethod.POST, path = "/cities", beanClass = CitiesHandler.class, beanMethod = "addCity"),
            @RouterOperation(method = RequestMethod.GET, path = "/cities/name/{name}", beanClass = CitiesHandler.class, beanMethod = "findByName"),
            @RouterOperation(method = RequestMethod.GET, path = "/cities", beanClass = CitiesHandler.class, beanMethod = "getAll"),
            @RouterOperation(method = RequestMethod.POST, path = "/cities", beanClass = CitiesHandler.class, beanMethod = "addCinemaToCity")
    })
    @Bean
    public RouterFunction<ServerResponse> citiesRouting(CitiesHandler citiesHandler) {
        return route(POST("/cities").and(accept(MediaType.APPLICATION_JSON)), citiesHandler::addCity)
                .andRoute(GET("/cities/name/{name}").and(accept(MediaType.APPLICATION_JSON)), citiesHandler::findByName)
                .andRoute(GET("/cities").and(accept(MediaType.APPLICATION_JSON)), citiesHandler::getAll)
                .andRoute(PUT("/cities").and(accept(MediaType.APPLICATION_JSON)), citiesHandler::addCinemaToCity);
    }

    @RouterOperations({
            @RouterOperation(method = RequestMethod.GET, path = "/statistics/cities/cinemaFrequency", beanClass = StatisticsHandler.class, beanMethod = "getCinemaFrequencyByCityForAllCities"),
            @RouterOperation(method = RequestMethod.GET, path = "/statistics/cities/cinemaFrequency/max", beanClass = StatisticsHandler.class, beanMethod = "getCinemaWithMaxFrequency"),
            @RouterOperation(method = RequestMethod.GET, path = "/statistics/movies/mostPopular/byCity", beanClass = StatisticsHandler.class, beanMethod = "findMostPopularMovieGroupedByCity"),
            @RouterOperation(method = RequestMethod.GET, path = "/statistics/movies/frequency", beanClass = StatisticsHandler.class, beanMethod = "findAllMoviesFrequency"),
            @RouterOperation(method = RequestMethod.GET, path = "/statistics/movies/mostPopularGroupedByGenre/byCity/{city}", beanClass = StatisticsHandler.class, beanMethod = "findMostPopularMoviesGroupedByGenreInCity"),
            @RouterOperation(method = RequestMethod.GET, path = "/statistics/averageTicketPrice", beanClass = StatisticsHandler.class, beanMethod = "getAverageTicketPriceGroupedByCity")
    })
    @Bean
    public RouterFunction<ServerResponse> statisticRouting(StatisticsHandler statisticsHandler) {
        return route(GET("/statistics/cities/cinemaFrequency").and(accept(MediaType.APPLICATION_JSON)), statisticsHandler::getCinemaFrequencyByCityForAllCities)
                .andRoute(GET("/statistics/cities/cinemaFrequency/max").and(accept(MediaType.APPLICATION_JSON)), statisticsHandler::getCityWithMaxFrequency)
                .andRoute(GET("/statistics/movies/mostPopular/byCity").and(accept(MediaType.APPLICATION_JSON)), statisticsHandler::findMostPopularMovieGroupedByCity)
                .andRoute(GET("/statistics/movies/frequency").and(accept(MediaType.APPLICATION_JSON)), statisticsHandler::findAllMoviesFrequency)
                .andRoute(GET("/statistics/movies/mostPopularGroupedByGenre/byCity/{city}").and(accept(MediaType.APPLICATION_JSON)), statisticsHandler::findMostPopularMoviesGroupedByGenreInCity)
                .andRoute(GET("/statistics/averageTicketPrice").and(accept(MediaType.APPLICATION_JSON)), statisticsHandler::getAverageTicketPriceGroupedByCity);
    }

    @Bean
    @RouterOperations({
            @RouterOperation(method = RequestMethod.POST, path = "/ticketPurchases/ticketOrderId/{ticketOrderId}", beanClass = TicketPurchaseHandler.class, beanMethod = "purchaseTicketFromOrder"),
            @RouterOperation(method = RequestMethod.POST, path = "/ticketPurchases", beanClass = TicketPurchaseHandler.class, beanMethod = "purchaseTicket"),
            @RouterOperation(method = RequestMethod.GET, path = "/ticketPurchases", beanClass = TicketPurchaseHandler.class, beanMethod = "getAllTicketPurchasesForLoggedUser"),
            @RouterOperation(method = RequestMethod.GET, path = "/ticketPurchases/city/{city}", beanClass = TicketPurchaseHandler.class, beanMethod = "getAllTicketPurchasesForLoggedUserByCityName"),
            @RouterOperation(method = RequestMethod.GET, path = "/ticketPurchases/cinemaId/{cinemaId}", beanClass = TicketPurchaseHandler.class, beanMethod = "getAllTicketPurchasesForUserByCinemaId"),
            @RouterOperation(method = RequestMethod.GET, path = "/admin/ticketPurchases/cinemaId/{cinemaId}", beanClass = TicketPurchaseHandler.class, beanMethod = "getAllTicketPurchasesByCinemaId"),
            @RouterOperation(method = RequestMethod.GET, path = "/admin/ticketPurchases/city/{city}", beanClass = TicketPurchaseHandler.class, beanMethod = "getAllTicketPurchasesByCity"),
            @RouterOperation(method = RequestMethod.GET, path = "/admin/ticketPurchases", beanClass = TicketPurchaseHandler.class, beanMethod = "getAllTicketPurchases"),
            @RouterOperation(method = RequestMethod.GET, path = "/admin/ticketPurchases/dates", beanClass = TicketPurchaseHandler.class, beanMethod = "getAllTicketPurchasesByDate"),
            @RouterOperation(method = RequestMethod.GET, path = "/admin/ticketPurchases/movieId/{movieId}", beanClass = TicketPurchaseHandler.class, beanMethod = "getAllTicketPurchasesWithMovieId"),
            @RouterOperation(method = RequestMethod.GET, path = "/ticketPurchases/movieId/{movieId}", beanClass = TicketPurchaseHandler.class, beanMethod = "getAllTicketPurchasesWithMovieIdForLoggedUser"),
            @RouterOperation(method = RequestMethod.GET, path = "/admin/ticketPurchases/cinemaHallId/{cinemaHallId}", beanClass = TicketPurchaseHandler.class, beanMethod = "getAllTicketPurchasesByCinemaHallId")
    })
    public RouterFunction<ServerResponse> ticketPurchasesRouting(TicketPurchaseHandler ticketPurchaseHandler) {
        return route(POST("/ticketPurchases/ticketOrderId/{ticketOrderId}").and(accept(MediaType.APPLICATION_JSON)), ticketPurchaseHandler::purchaseTicketFromOrder)
                .andRoute(POST("/ticketPurchases").and(accept(MediaType.APPLICATION_JSON)), ticketPurchaseHandler::purchaseTicket)
                .andRoute(GET("/ticketPurchases").and(accept(MediaType.APPLICATION_JSON)), ticketPurchaseHandler::getAllTicketPurchasesForLoggedUser)
                .andRoute(GET("/ticketPurchases/city/{city}").and(accept(MediaType.APPLICATION_JSON)), ticketPurchaseHandler::getAllTicketPurchasesForLoggedUserByCityName)
                .andRoute(GET("/ticketPurchases/cinemaId/{cinemaId}").and(accept(MediaType.APPLICATION_JSON)), ticketPurchaseHandler::getAllTicketPurchasesForUserByCinemaId)
                .andRoute(GET("/admin/ticketPurchases/cinemaId/{cinemaId}").and(accept(MediaType.APPLICATION_JSON)), ticketPurchaseHandler::getAllTicketPurchasesByCinemaId)
                .andRoute(GET("/admin/ticketPurchases/city/{city}").and(accept(MediaType.APPLICATION_JSON)), ticketPurchaseHandler::getAllTicketPurchasesByCity)
                .andRoute(GET("/admin/ticketPurchases").and(accept(MediaType.APPLICATION_JSON)), ticketPurchaseHandler::getAllTicketPurchases)
                .andRoute(GET("/admin/ticketPurchases/dates").and(accept(MediaType.APPLICATION_JSON)), ticketPurchaseHandler::getAllTicketPurchasesByDate)
                .andRoute(GET("/admin/ticketPurchases/movieId/{movieId}").and(accept(MediaType.APPLICATION_JSON)), ticketPurchaseHandler::getAllTicketPurchasesWithMovieId)
                .andRoute(GET("/ticketPurchases/movieId/{movieId}").and(accept(MediaType.APPLICATION_JSON)), ticketPurchaseHandler::getAllTicketPurchasesWithMovieIdForLoggedUser)
                .andRoute(GET("/admin/ticketPurchases/cinemaHallId/{cinemaHallId}").and(accept(MediaType.APPLICATION_JSON)), ticketPurchaseHandler::getAllTicketPurchasesByCinemaHallId);
    }
}
