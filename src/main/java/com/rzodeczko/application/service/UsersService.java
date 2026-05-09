package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.CreateUserDto;
import com.rzodeczko.application.dto.UserDto;
import com.rzodeczko.application.exception.RegistrationUserException;
import com.rzodeczko.application.exception.UserServiceException;
import com.rzodeczko.application.mapper.UserMapper;
import com.rzodeczko.application.port.out.PasswordEncoderPort;
import com.rzodeczko.application.port.out.TransactionPort;
import com.rzodeczko.application.port.out.UserPort;
import com.rzodeczko.application.service.enums.UserField;
import com.rzodeczko.application.validator.CreateUserDtoValidator;
import com.rzodeczko.application.validator.util.Validations;
import com.rzodeczko.domain.security.User;
import com.rzodeczko.domain.security.enums.Role;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Function;

import static java.util.Objects.nonNull;

public class UsersService {

    private final UserPort userPort;
    private final CreateUserDtoValidator createUserDtoValidator;
    private final PasswordEncoderPort passwordEncoder;
    private final TransactionPort transactionPort;

    public UsersService(UserPort userPort,
                        CreateUserDtoValidator createUserDtoValidator,
                        PasswordEncoderPort passwordEncoder,
                        TransactionPort transactionPort) {
        this.userPort = userPort;
        this.createUserDtoValidator = createUserDtoValidator;
        this.passwordEncoder = passwordEncoder;
        this.transactionPort = transactionPort;
    }

    public Mono<UserDto> register(final CreateUserDto createUserDto) {
        var errors = createUserDtoValidator.validate(createUserDto);

        if (Validations.hasErrors(errors)) {
            return Mono.error(() -> new RegistrationUserException(Validations.createErrorMessage(errors)));
        }

        return returnMonoErrorIfExists(userPort::findByUsername, UserField.USERNAME, createUserDto.username())
                .then(returnMonoErrorIfExists(userPort::findByEmail, UserField.EMAIL, createUserDto.email()))
                .then(createUser(createUserDto).map(UserMapper::toDto));
    }

    public Flux<UserDto> getAll() {
        return userPort
                .findAll()
                .map(UserMapper::toDto);
    }

    public Mono<UserDto> getByUsername(String username) {
        return userPort
                .findByUsername(username)
                .switchIfEmpty(Mono.error(() -> new UserServiceException("No user with username: %s".formatted(username))))
                .map(UserMapper::toDto);
    }

    /**
     * Promotes an existing user to ADMIN role. Implementation only updates the {@code role} field
     * of the existing document — the user keeps the same id, email, favorite movies and history.
     * This is wrapped in a transaction so that any concurrent updates fail fast.
     */
    public Mono<UserDto> promoteUserToAdminRole(String username) {
        Mono<User> result = userPort
                .findByUsername(username)
                .switchIfEmpty(Mono.error(() -> new UserServiceException(
                        "No user with username: %s".formatted(username))))
                .flatMap(user -> {
                    if (user.getRole() == Role.ROLE_ADMIN) {
                        return Mono.error(new UserServiceException(
                                "User with username: %s is already an admin".formatted(username)));
                    }
                    user.setRole(Role.ROLE_ADMIN);
                    return userPort.addOrUpdate(user);
                });

        return transactionPort.inTransaction(result).map(UserMapper::toDto);
    }

    private Mono<?> returnMonoErrorIfExists(Function<String, Mono<User>> function, UserField userField, String arg) {
        return function.apply(arg)
                .flatMap(user -> Mono.<Void>error(
                        new RegistrationUserException(
                                "User with %s: %s already exists".formatted(userField.getDesc(), arg))));
    }

    private Mono<User> createUser(final CreateUserDto createUserDto) {
        return Mono.fromCallable(() -> nonNull(createUserDto.password())
                        ? passwordEncoder.encode(createUserDto.password())
                        : null)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(encodedPassword -> userPort
                        .addOrUpdate(createUserDto
                                .withPassword(encodedPassword)
                                .toEntity()));
    }
}
