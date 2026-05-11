package com.rzodeczko.infrastructure.config;

import com.rzodeczko.application.port.out.*;
import com.rzodeczko.application.service.*;
import com.rzodeczko.application.validator.*;
import com.rzodeczko.infrastructure.csv.CsvMovieParserAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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
    public CinemaHallService cinemaHallService(CinemaHallPort cinemaHallPort,
                                               CinemaPort cinemaPort,
                                               TransactionPort transactionPort) {
        return new CinemaHallService(cinemaHallPort, cinemaPort, transactionPort);
    }

    @Bean
    public CinemaService cinemaService(CinemaPort cinemaPort,
                                       CinemaHallPort cinemaHallPort,
                                       CityPort cityPort,
                                       CreateCinemaDtoValidator createCinemaDtoValidator,
                                       TransactionPort transactionPort) {
        return new CinemaService(cinemaPort, cinemaHallPort, cityPort,
                createCinemaDtoValidator, transactionPort);
    }

    @Bean
    public CityService cityService(CityPort cityPort,
                                   CinemaPort cinemaPort,
                                   CinemaHallPort cinemaHallPort,
                                   TransactionPort transactionPort) {
        return new CityService(cityPort, cinemaPort, cinemaHallPort, transactionPort);
    }

    @Bean
    public EmailService emailService(MailPort mailPort,
                                     CreateMailDtoValidator createMailDtoValidator,
                                     CreateMailsDtoValidator createMailsDtoValidator,
                                     SendEmailToSelfDtoValidator sendEmailToSelfDtoValidator) {
        return new EmailService(mailPort, createMailDtoValidator, createMailsDtoValidator, sendEmailToSelfDtoValidator);
    }

    @Bean
    public MovieEmissionService movieEmissionService(MovieEmissionPort movieEmissionPort,
                                                     CinemaHallPort cinemaHallPort,
                                                     MoviePort moviePort,
                                                     TransactionPort transactionPort) {
        return new MovieEmissionService(movieEmissionPort, cinemaHallPort, moviePort,
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
    public StatisticsService statisticsService(TicketPurchasePort ticketPurchasePort,
                                               CityPort cityPort,
                                               MoviePort moviePort) {
        return new StatisticsService(ticketPurchasePort, cityPort, moviePort);
    }

    @Bean
    public TicketOrderService ticketOrderService(TicketOrderPort ticketOrderPort,
                                                 MovieEmissionPort movieEmissionPort,
                                                 UserPort userPort,
                                                 TicketPort ticketPort,
                                                 CreateTicketsOrderDtoValidator createTicketsOrderDtoValidator,
                                                 TransactionPort transactionPort) {
        return new TicketOrderService(ticketOrderPort, movieEmissionPort, userPort,
                ticketPort, createTicketsOrderDtoValidator, transactionPort);
    }

    @Bean
    public TicketPurchaseService ticketPurchaseService(TicketPurchasePort ticketPurchasePort,
                                                       CreateTicketPurchaseDtoValidator createTicketPurchaseDtoValidator,
                                                       MovieEmissionPort movieEmissionPort,
                                                       MoviePort moviePort,
                                                       UserPort userPort,
                                                       CinemaHallPort cinemaHallPort,
                                                       TicketPort ticketPort,
                                                       CityPort cityPort,
                                                       TicketOrderPort ticketOrderPort,
                                                       CinemaPort cinemaPort,
                                                       TransactionPort transactionPort) {
        return new TicketPurchaseService(ticketPurchasePort, createTicketPurchaseDtoValidator,
                movieEmissionPort, moviePort, userPort, cinemaHallPort,
                ticketPort, cityPort, ticketOrderPort, cinemaPort,
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