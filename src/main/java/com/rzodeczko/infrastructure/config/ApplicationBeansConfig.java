package com.rzodeczko.infrastructure.config;

import com.rzodeczko.application.port.out.*;
import com.rzodeczko.application.service.*;
import com.rzodeczko.application.validator.*;
import com.rzodeczko.infrastructure.csv.CsvMovieParserAdapter;
import com.rzodeczko.infrastructure.security.tokens.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class})
public class ApplicationBeansConfig {

    @Bean
    public AddCinemaHallToCinemaDtoValidator addCinemaHallToCinemaDtoValidator() {
        return new AddCinemaHallToCinemaDtoValidator();
    }

    @Bean
    public CreateCinemaDtoValidator createCinemaDtoValidator() {
        return new CreateCinemaDtoValidator();
    }

    @Bean
    public CreateCinemaHallDtoValidator createCinemaHallDtoValidator() {
        return new CreateCinemaHallDtoValidator();
    }

    @Bean
    public CreateMailDtoValidator createMailDtoValidator() {
        return new CreateMailDtoValidator();
    }

    @Bean
    public CreateMailsDtoValidator createMailsDtoValidator() {
        return new CreateMailsDtoValidator();
    }

    @Bean
    public SendEmailToSelfDtoValidator sendEmailToSelfDtoValidator() {
        return new SendEmailToSelfDtoValidator();
    }

    @Bean
    public CreateMovieDtoValidator createMovieDtoValidator() {
        return new CreateMovieDtoValidator();
    }

    @Bean
    public CreateTicketPurchaseDtoValidator createTicketPurchaseDtoValidator() {
        return new CreateTicketPurchaseDtoValidator();
    }

    @Bean
    public CreateTicketsOrderDtoValidator createTicketsOrderDtoValidator() {
        return new CreateTicketsOrderDtoValidator();
    }

    @Bean
    public CreateUserDtoValidator createUserDtoValidator() {
        return new CreateUserDtoValidator();
    }

    @Bean
    public CinemaHallService cinemaHallService(CinemaHallPort CinemaHallPort,
                                               CinemaPort CinemaPort,
                                               TransactionPort transactionPort) {
        return new CinemaHallService(CinemaHallPort, CinemaPort, transactionPort);
    }

    @Bean
    public CinemaService cinemaService(CinemaPort CinemaPort,
                                       CinemaHallPort CinemaHallPort,
                                       CityPort CityPort,
                                       CreateCinemaDtoValidator createCinemaDtoValidator,
                                       TransactionPort transactionPort) {
        return new CinemaService(CinemaPort, CinemaHallPort, CityPort,
                createCinemaDtoValidator, transactionPort);
    }

    @Bean
    public CityService cityService(CityPort CityPort,
                                   CinemaPort CinemaPort,
                                   CinemaHallPort CinemaHallPort,
                                   TransactionPort transactionPort) {
        return new CityService(CityPort, CinemaPort, CinemaHallPort, transactionPort);
    }

    @Bean
    public EmailService emailService(MailPort mailPort,
                                     CreateMailDtoValidator createMailDtoValidator,
                                     CreateMailsDtoValidator createMailsDtoValidator,
                                     SendEmailToSelfDtoValidator sendEmailToSelfDtoValidator) {
        return new EmailService(mailPort, createMailDtoValidator, createMailsDtoValidator, sendEmailToSelfDtoValidator);
    }

    @Bean
    public MovieEmissionService movieEmissionService(MovieEmissionPort MovieEmissionPort,
                                                     CinemaHallPort CinemaHallPort,
                                                     MoviePort MoviePort,
                                                     TransactionPort transactionPort) {
        return new MovieEmissionService(MovieEmissionPort, CinemaHallPort, MoviePort,
                transactionPort);
    }

    @Bean
    public MovieService movieService(MoviePort moviePort,
                                     UserPort userPort,
                                     CreateMovieDtoValidator createMovieDtoValidator,
                                     MovieCsvParserPort movieCsvParserPort,
                                     TransactionPort transactionPort) {
        return new MovieService(moviePort, userPort, createMovieDtoValidator, movieCsvParserPort, transactionPort);
    }

    @Bean
    public MovieCsvParserPort movieCsvParserPort(CreateMovieDtoValidator createMovieDtoValidator) {
        return new CsvMovieParserAdapter(createMovieDtoValidator);
    }

    @Bean
    public StatisticsService statisticsService(TicketPurchasePort TicketPurchasePort,
                                               CityPort CityPort,
                                               MoviePort MoviePort) {
        return new StatisticsService(TicketPurchasePort, CityPort, MoviePort);
    }

    @Bean
    public TicketOrderService ticketOrderService(TicketOrderPort TicketOrderPort,
                                                 MovieEmissionPort MovieEmissionPort,
                                                 UserPort UserPort,
                                                 TicketPort TicketPort,
                                                 CreateTicketsOrderDtoValidator createTicketsOrderDtoValidator,
                                                 TransactionPort transactionPort) {
        return new TicketOrderService(TicketOrderPort, MovieEmissionPort, UserPort,
                TicketPort, createTicketsOrderDtoValidator, transactionPort);
    }

    @Bean
    public TicketPurchaseService ticketPurchaseService(TicketPurchasePort TicketPurchasePort,
                                                       CreateTicketPurchaseDtoValidator createTicketPurchaseDtoValidator,
                                                       MovieEmissionPort MovieEmissionPort,
                                                       MoviePort MoviePort,
                                                       UserPort UserPort,
                                                       CinemaHallPort CinemaHallPort,
                                                       TicketPort TicketPort,
                                                       CityPort CityPort,
                                                       TicketOrderPort TicketOrderPort,
                                                       CinemaPort CinemaPort,
                                                       TransactionPort transactionPort) {
        return new TicketPurchaseService(TicketPurchasePort, createTicketPurchaseDtoValidator,
                MovieEmissionPort, MoviePort, UserPort, CinemaHallPort,
                TicketPort, CityPort, TicketOrderPort, CinemaPort,
                transactionPort);
    }

    @Bean
    public UsersService usersService(UserPort userPort,
                                     CreateUserDtoValidator createUserDtoValidator,
                                     PasswordEncoderPort passwordEncoder,
                                     TransactionPort transactionPort) {
        return new UsersService(userPort, createUserDtoValidator, passwordEncoder, transactionPort);
    }
}