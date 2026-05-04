package com.app.application.service;

import com.app.application.dto.CreateUserDto;
import com.app.application.dto.UserDto;
import com.app.application.exception.RegistrationUserException;
import com.app.application.exception.UserServiceException;
import com.app.application.service.enums.UserField;
import com.app.application.validator.CreateUserDtoValidator;
import com.app.application.validator.util.Validations;
import com.app.domain.security.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsersService {

    private final UserRepository userRepository;
    private final CreateUserDtoValidator createUserDtoValidator;
    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;
    private final TransactionalOperator transactionalOperator;

    public Mono<UserDto> register(final CreateUserDto createUserDto) {
        var errors = createUserDtoValidator.validate(createUserDto);

        if (Validations.hasErrors(errors)) {
            return Mono.error(() -> new RegistrationUserException(Validations.createErrorMessage(errors)));
        }

        return returnMonoErrorIfExists(userRepository::findByUsername, UserField.USERNAME, createUserDto.getUsername())
                .then(returnMonoErrorIfExists(userRepository::findByEmail, UserField.EMAIL, createUserDto.getEmail()))
                .then(createUser(createUserDto).map(User::toDto));
    }

    private Mono<?> returnMonoErrorIfExists(Function<String, Mono<User>> function, UserField userField, String arg) {
        return function.apply(arg)
                .flatMap(user -> Mono.<Void>error(
                        new RegistrationUserException(
                                "User with %s: %s already exists".formatted(userField.getDesc(), arg))));
    }

    private Mono<User> createUser(final CreateUserDto createUserDto) {
        return userRepository
                .addOrUpdate(createUserDto
                        .setPassword(nonNull(createUserDto.getPassword())
                                ? passwordEncoder.encode(createUserDto.getPassword())
                                : null)
                        .toEntity());
    }

    public Flux<UserDto> getAll() {
        return userRepository
                .findAll()
                .map(User::toDto);
    }

    public Mono<UserDto> getByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .switchIfEmpty(Mono.error(() -> new UserServiceException("No user with username: %s".formatted(username))))
                .map(User::toDto);
    }

    public Mono<UserDto> promoteUserToAdminRole(String username) {
        return userRepository
                .findByUsername(username)
                .switchIfEmpty(Mono.error(() -> new UserServiceException("No user with username: %s".formatted(username))))
                .flatMap(user -> userRepository.deleteById(user.getId()))
                .flatMap(user -> adminRepository
                        .addOrUpdate(promoteUserToAdmin(user))
                        .map(Admin::toUserDto)
                )
                .as(transactionalOperator::transactional);
    }

    private Admin promoteUserToAdmin(User user) {
        return Optional.ofNullable(user)
                .map(userVal -> new Admin(user.getUsername(), userVal.getPassword()))
                .orElseThrow(() -> new UserServiceException("User cannot be null during promotion"));
    }
}
