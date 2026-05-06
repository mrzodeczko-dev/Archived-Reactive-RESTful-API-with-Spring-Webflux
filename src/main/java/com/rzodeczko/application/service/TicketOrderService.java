package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.CreateTicketOrderDto;
import com.rzodeczko.application.dto.TicketDetailsDto;
import com.rzodeczko.application.dto.TicketOrderDto;
import com.rzodeczko.application.exception.TicketOrderServiceException;
import com.rzodeczko.application.mapper.TicketOrderMapper;
import com.rzodeczko.application.port.out.MovieEmissionPort;
import com.rzodeczko.application.port.out.TicketOrderPort;
import com.rzodeczko.application.port.out.TicketPort;
import com.rzodeczko.application.port.out.TransactionPort;
import com.rzodeczko.application.port.out.UserPort;
import com.rzodeczko.application.validator.CreateTicketsOrderDtoValidator;
import com.rzodeczko.application.validator.util.Validations;
import com.rzodeczko.domain.ticket.Ticket;
import com.rzodeczko.domain.ticket.enums.TicketStatus;
import com.rzodeczko.domain.ticket_order.TicketOrder;
import com.rzodeczko.domain.ticket_order.enums.TicketOrderStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public class TicketOrderService {

    private final TicketOrderPort ticketOrderPort;
    private final MovieEmissionPort movieEmissionPort;
    private final UserPort userPort;
    private final TicketPort ticketPort;
    private final CreateTicketsOrderDtoValidator createTicketsOrderDtoValidator;
    private final TransactionPort transactionPort;

    public TicketOrderService(TicketOrderPort ticketOrderPort, MovieEmissionPort movieEmissionPort,
                              UserPort userPort, TicketPort ticketPort,
                              CreateTicketsOrderDtoValidator createTicketsOrderDtoValidator,
                              TransactionPort transactionPort) {
        this.ticketOrderPort = ticketOrderPort;
        this.movieEmissionPort = movieEmissionPort;
        this.userPort = userPort;
        this.ticketPort = ticketPort;
        this.createTicketsOrderDtoValidator = createTicketsOrderDtoValidator;
        this.transactionPort = transactionPort;
    }

    public Mono<TicketOrderDto> addTicketOrder(Mono<? extends Principal> principal, CreateTicketOrderDto createTicketOrderDto) {

        var errors = createTicketsOrderDtoValidator.validate(createTicketOrderDto);
        if (Validations.hasErrors(errors)) {
            return Mono.error(() -> new TicketOrderServiceException(
                    "Validation errors: %s".formatted(Validations.createErrorMessage(errors))));
        }

        Mono<TicketOrder> result = principal
                .flatMap(p -> movieEmissionPort
                        .findById(createTicketOrderDto.getMovieEmissionId())
                        .switchIfEmpty(Mono.error(() -> new TicketOrderServiceException(
                                "No movie emission with id: %s".formatted(createTicketOrderDto.getMovieEmissionId()))))
                        .flatMap(movieEmission ->
                                createTicketOrderDto.areAllPositionsAvailable(movieEmission.getFreePositions())
                                        ? Mono.just(movieEmission)
                                        : Mono.error(new TicketOrderServiceException("Positions are not valid")))
                        .flatMap(movieEmission -> movieEmissionPort
                                .addOrUpdate(movieEmission.removeFreePositions(
                                        createTicketOrderDto.getTicketsDetails().stream()
                                                .map(TicketDetailsDto::getPosition)
                                                .collect(Collectors.toList())))
                                .flatMap(ignored -> userPort.findByUsername(p.getName()))
                                .map(user -> TicketOrder.builder()
                                        .orderDate(LocalDate.now())
                                        .movieEmission(movieEmission)
                                        .ticketGroupType(createTicketOrderDto.getTicketGroupType())
                                        .ticketOrderStatus(TicketOrderStatus.ORDERED)
                                        .user(user)
                                        .tickets(createTicketOrderDto.getTicketsDetails().stream()
                                                .map(td -> Ticket.builder()
                                                        .position(td.getPosition())
                                                        .type(td.getIndividualTicketType())
                                                        .ticketStatus(TicketStatus.PURCHASED)
                                                        .discount(createTicketOrderDto.getBaseDiscount()
                                                                .add(td.getIndividualTicketType().getDiscount()))
                                                        .build())
                                                .toList())
                                        .build())))
                .flatMap(ticketOrder -> ticketPort.addOrUpdateMany(ticketOrder.getTickets())
                        .collectList()
                        .flatMap(savedTickets -> ticketOrderPort.addOrUpdate(
                                ticketOrder.toBuilder().tickets(savedTickets).build())));

        return transactionPort.inTransaction(result).map(TicketOrderMapper::toDto);
    }

    public Mono<TicketOrderDto> cancelOrder(String username, String orderId) {
        if (isNull(orderId)) {
            return Mono.error(new TicketOrderServiceException("Order id is null"));
        }

        return ticketOrderPort.findById(orderId)
                .switchIfEmpty(Mono.error(new TicketOrderServiceException("No order with id: %s".formatted(orderId))))
                .flatMap(ticketOrder -> {
                    if (!Objects.equals(ticketOrder.getUser().getUsername(), username)) {
                        return Mono.error(new TicketOrderServiceException("That ticket order does not belong to you"));
                    }
                    return Mono.just(ticketOrder.changeOrderStatusToCancelled());
                })
                .map(TicketOrderMapper::toDto);
    }

    public Flux<TicketOrderDto> getAllTicketOrdersForLoggedUser(String username) {
        return ticketOrderPort.findAllByUsername(username).map(TicketOrderMapper::toDto);
    }
}
