package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.CreateUserDto;
import com.rzodeczko.application.dto.UserDto;
import com.rzodeczko.application.exception.RegistrationUserException;
import com.rzodeczko.application.exception.UserServiceException;
import com.rzodeczko.application.mapper.UserMapper;
import com.rzodeczko.application.port.out.AdminPort;
import com.rzodeczko.application.port.out.TransactionPort;
import com.rzodeczko.application.port.out.UserPort;
import com.rzodeczko.application.service.enums.UserField;
import com.rzodeczko.application.validator.CreateUserDtoValidator;
import com.rzodeczko.application.validator.util.Validations;
import com.rzodeczko.domain.security.Admin;
import com.rzodeczko.domain.security.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
@Slf4j
public class UsersService {

    private final UserPort userPort;
    private final CreateUserDtoValidator createUserDtoValidator;
    private final PasswordEncoder passwordEncoder;
    private final AdminPort adminPort;
    private final TransactionPort transactionPort;

    public Mono<UserDto> register(final CreateUserDto createUserDto) {
        var errors = createUserDtoValidator.validate(createUserDto);

        if (Validations.hasErrors(errors)) {
            return Mono.error(() -> new RegistrationUserException(Validations.createErrorMessage(errors)));
        }

        return returnMonoErrorIfExists(userPort::findByUsername, UserField.USERNAME, createUserDto.getUsername())
                .then(returnMonoErrorIfExists(userPort::findByEmail, UserField.EMAIL, createUserDto.getEmail()))
                .then(createUser(createUserDto).map(UserMapper::toDto));
    }

    private Mono<?> returnMonoErrorIfExists(Function<String, Mono<User>> function, UserField userField, String arg) {
        return function.apply(arg)
                .flatMap(user -> Mono.<Void>error(
                        new RegistrationUserException(
                                "User with %s: %s already exists".formatted(userField.getDesc(), arg))));
    }

    private Mono<User> createUser(final CreateUserDto createUserDto) {
        return Mono.fromCallable(() -> nonNull(createUserDto.getPassword())
                        ? passwordEncoder.encode(createUserDto.getPassword())
                        : null)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(encodedPassword -> userPort
                        .addOrUpdate(createUserDto
                                .setPassword(encodedPassword)
                                .toEntity()));
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

    public Mono<UserDto> promoteUserToAdminRole(String username) {
        return userPort
                .findByUsername(username)
                .switchIfEmpty(Mono.error(() -> new UserServiceException("No user with username: %s".formatted(username))))
                .flatMap(user -> userPort.deleteById(user.getId()))
                .flatMap(user -> adminPort
                        .addOrUpdate(promoteUserToAdmin(user))
                        .map(UserMapper::toDto)
                )
                .as(transactionPort::inTransaction);
    }

    private Admin promoteUserToAdmin(User user) {
        return Optional.ofNullable(user)
                .map(userVal -> new Admin(user.getUsername(), userVal.getPassword()))
                .orElseThrow(() -> new UserServiceException("User cannot be null during promotion"));
    }
}
