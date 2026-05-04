package com.app.application.service;

import com.app.application.dto.CreateUserDto;
import com.app.application.dto.UserDto;
import com.app.application.exception.RegistrationUserException;
import com.app.application.exception.UserServiceException;
import com.app.application.validator.CreateUserDtoValidator;
import com.app.domain.security.Admin;
import com.app.domain.security.AdminRepository;
import com.app.domain.security.User;
import com.app.domain.security.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CreateUserDtoValidator createUserDtoValidator;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AdminRepository adminRepository;
    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private UsersService usersService;

    private User userJan;
    private CreateUserDto validCreateDto;

    @BeforeEach
    void setUp() {
        userJan = User.builder()
                .username("jan@example.com")
                .email("jan@example.com")
                .password("hashed-pass")
                .birthDate(LocalDate.of(1995, 5, 20))
                .build();

        validCreateDto = CreateUserDto.builder()
                .username("jan@example.com")
                .email("jan@example.com")
                .password("Secret123!")
                .birthDate("20-05-1995")
                .build();
    }

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("Happy path: new user registered, password encoded")
        void shouldRegisterSuccessfully() {
            when(createUserDtoValidator.validate(validCreateDto)).thenReturn(Map.of());
            when(userRepository.findByUsername("jan@example.com")).thenReturn(Mono.empty());
            when(userRepository.findByEmail("jan@example.com")).thenReturn(Mono.empty());
            when(passwordEncoder.encode("Secret123!")).thenReturn("hashed-pass");
            when(userRepository.addOrUpdate(any())).thenReturn(Mono.just(userJan));

            StepVerifier.create(usersService.register(validCreateDto))
                    .assertNext(dto -> assertThat(dto.getUsername()).isEqualTo("jan@example.com"))
                    .verifyComplete();

            verify(passwordEncoder).encode("Secret123!");
        }

        @Test
        @DisplayName("Validation error: RegistrationUserException emitted, no DB calls")
        void shouldErrorWhenValidationFails() {
            when(createUserDtoValidator.validate(validCreateDto))
                    .thenReturn(Map.of("email", "must not be blank"));

            StepVerifier.create(usersService.register(validCreateDto))
                    .expectErrorSatisfies(ex -> assertThat(ex).isInstanceOf(RegistrationUserException.class))
                    .verify();

            verifyNoInteractions(userRepository, passwordEncoder);
        }

        @Test
        @DisplayName("Username already exists: RegistrationUserException with username")
        void shouldErrorWhenUsernameExists() {
            // Service queries by the DTO username, not a hard-coded one.
            when(createUserDtoValidator.validate(validCreateDto)).thenReturn(Map.of());
            when(userRepository.findByUsername("jan@example.com"))
                    .thenReturn(Mono.just(userJan));
            // Reactor evaluates the `.then(...)` chain eagerly to build the pipeline,
            // so downstream stubs must return non-null Monos even though they aren't subscribed.
            // Stubbed leniently because they may not be invoked depending on the early termination point.
            org.mockito.Mockito.lenient().when(userRepository.findByEmail("jan@example.com")).thenReturn(Mono.empty());
            org.mockito.Mockito.lenient().when(userRepository.addOrUpdate(any())).thenReturn(Mono.just(userJan));

            StepVerifier.create(usersService.register(validCreateDto))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(RegistrationUserException.class);
                        assertThat(ex.getMessage()).contains("jan@example.com");
                    })
                    .verify();
        }

        @Test
        @DisplayName("Email already exists: RegistrationUserException with email")
        void shouldErrorWhenEmailExists() {
            when(createUserDtoValidator.validate(validCreateDto)).thenReturn(Map.of());
            when(userRepository.findByUsername("jan@example.com")).thenReturn(Mono.empty());
            when(userRepository.findByEmail("jan@example.com")).thenReturn(Mono.just(userJan));
            // The chain of Mono.then(...) evaluates createUser eagerly to build the pipeline,
            // so addOrUpdate must return a non-null Mono even though it should never be subscribed.
            org.mockito.Mockito.lenient().when(userRepository.addOrUpdate(any())).thenReturn(Mono.just(userJan));

            StepVerifier.create(usersService.register(validCreateDto))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(RegistrationUserException.class);
                        assertThat(ex.getMessage()).contains("jan@example.com");
                    })
                    .verify();
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAllTests {

        @Test
        @DisplayName("Two users: Flux emits two DTOs")
        void shouldReturnAllUsers() {
            User user2 = User.builder().username("anna@example.com").email("anna@example.com").password("hash").build();
            when(userRepository.findAll()).thenReturn(Flux.just(userJan, user2));

            StepVerifier.create(usersService.getAll())
                    .assertNext(dto -> assertThat(dto.getUsername()).isEqualTo("jan@example.com"))
                    .assertNext(dto -> assertThat(dto.getUsername()).isEqualTo("anna@example.com"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("No users: Flux completes empty")
        void shouldReturnEmptyFlux() {
            when(userRepository.findAll()).thenReturn(Flux.empty());
            StepVerifier.create(usersService.getAll()).verifyComplete();
        }
    }

    @Nested
    @DisplayName("getByUsername()")
    class GetByUsernameTests {

        @Test
        @DisplayName("Happy path: existing user returned as DTO")
        void shouldReturnUser() {
            when(userRepository.findByUsername("jan@example.com")).thenReturn(Mono.just(userJan));

            StepVerifier.create(usersService.getByUsername("jan@example.com"))
                    .assertNext(dto -> assertThat(dto.getUsername()).isEqualTo("jan@example.com"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("User not found: UserServiceException with username")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByUsername("ghost")).thenReturn(Mono.empty());

            StepVerifier.create(usersService.getByUsername("ghost"))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(UserServiceException.class);
                        assertThat(ex.getMessage()).contains("ghost");
                    })
                    .verify();
        }
    }

    @Nested
    @DisplayName("promoteUserToAdminRole()")
    class PromoteToAdminTests {

        @Test
        @DisplayName("Happy path: user deleted, admin created, AdminDto returned")
        void shouldPromoteUserToAdmin() {
            Admin admin = new Admin("jan@example.com", "hashed-pass");

            when(userRepository.findByUsername("jan@example.com")).thenReturn(Mono.just(userJan));
            when(userRepository.deleteById(userJan.getId())).thenReturn(Mono.just(userJan));
            when(adminRepository.addOrUpdate(any())).thenReturn(Mono.just(admin));
            when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(usersService.promoteUserToAdminRole("jan@example.com"))
                    .assertNext(dto -> assertThat(dto.getUsername()).isEqualTo("jan@example.com"))
                    .verifyComplete();

            verify(userRepository).deleteById(userJan.getId());
            verify(adminRepository).addOrUpdate(any());
        }

        @Test
        @DisplayName("User not found: UserServiceException with username")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByUsername("ghost")).thenReturn(Mono.empty());
            when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(usersService.promoteUserToAdminRole("ghost"))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(UserServiceException.class);
                        assertThat(ex.getMessage()).contains("ghost");
                    })
                    .verify();

            verifyNoInteractions(adminRepository);
        }

        @Test
        @DisplayName("Transaction: pipeline wrapped exactly once")
        void shouldWrapInTransaction() {
            Admin admin = new Admin("jan@example.com", "hashed-pass");
            when(userRepository.findByUsername("jan@example.com")).thenReturn(Mono.just(userJan));
            when(userRepository.deleteById(userJan.getId())).thenReturn(Mono.just(userJan));
            when(adminRepository.addOrUpdate(any())).thenReturn(Mono.just(admin));
            when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(usersService.promoteUserToAdminRole("jan@example.com"))
                    .expectNextCount(1)
                    .verifyComplete();

            verify(transactionalOperator, times(1)).transactional(any(Mono.class));
        }
    }
}
