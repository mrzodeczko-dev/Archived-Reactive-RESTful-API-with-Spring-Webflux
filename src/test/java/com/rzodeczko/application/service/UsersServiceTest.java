package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.CreateUserDto;
import com.rzodeczko.application.exception.RegistrationUserException;
import com.rzodeczko.application.exception.UserServiceException;
import com.rzodeczko.application.port.out.AdminPort;
import com.rzodeczko.application.port.out.TransactionPort;
import com.rzodeczko.application.port.out.UserPort;
import com.rzodeczko.application.service.UsersService;
import com.rzodeczko.application.validator.CreateUserDtoValidator;
import com.rzodeczko.domain.security.Admin;
import com.rzodeczko.domain.security.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersServiceTest {

    @Mock
    private UserPort userPort;
    @Mock
    private CreateUserDtoValidator createUserDtoValidator;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AdminPort adminPort;
    @Mock
    private TransactionPort transactionPort;

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
            when(userPort.findByUsername("jan@example.com")).thenReturn(Mono.empty());
            when(userPort.findByEmail("jan@example.com")).thenReturn(Mono.empty());
            when(passwordEncoder.encode("Secret123!")).thenReturn("hashed-pass");
            when(userPort.addOrUpdate(any())).thenReturn(Mono.just(userJan));

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

            verifyNoInteractions(userPort, passwordEncoder);
        }

        @Test
        @DisplayName("Username already exists: RegistrationUserException with username")
        void shouldErrorWhenUsernameExists() {
            when(createUserDtoValidator.validate(validCreateDto)).thenReturn(Map.of());
            when(userPort.findByUsername("jan@example.com"))
                    .thenReturn(Mono.just(userJan));

            org.mockito.Mockito.lenient().when(userPort.findByEmail("jan@example.com")).thenReturn(Mono.empty());
            org.mockito.Mockito.lenient().when(userPort.addOrUpdate(any())).thenReturn(Mono.just(userJan));

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
            when(userPort.findByUsername("jan@example.com")).thenReturn(Mono.empty());
            when(userPort.findByEmail("jan@example.com")).thenReturn(Mono.just(userJan));
            org.mockito.Mockito.lenient().when(userPort.addOrUpdate(any())).thenReturn(Mono.just(userJan));

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
            when(userPort.findAll()).thenReturn(Flux.just(userJan, user2));

            StepVerifier.create(usersService.getAll())
                    .assertNext(dto -> assertThat(dto.getUsername()).isEqualTo("jan@example.com"))
                    .assertNext(dto -> assertThat(dto.getUsername()).isEqualTo("anna@example.com"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("No users: Flux completes empty")
        void shouldReturnEmptyFlux() {
            when(userPort.findAll()).thenReturn(Flux.empty());
            StepVerifier.create(usersService.getAll()).verifyComplete();
        }
    }

    @Nested
    @DisplayName("getByUsername()")
    class GetByUsernameTests {

        @Test
        @DisplayName("Happy path: existing user returned as DTO")
        void shouldReturnUser() {
            when(userPort.findByUsername("jan@example.com")).thenReturn(Mono.just(userJan));

            StepVerifier.create(usersService.getByUsername("jan@example.com"))
                    .assertNext(dto -> assertThat(dto.getUsername()).isEqualTo("jan@example.com"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("User not found: UserServiceException with username")
        void shouldThrowWhenUserNotFound() {
            when(userPort.findByUsername("ghost")).thenReturn(Mono.empty());

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

            when(userPort.findByUsername("jan@example.com")).thenReturn(Mono.just(userJan));
            when(userPort.deleteById(userJan.getId())).thenReturn(Mono.just(userJan));
            when(adminPort.addOrUpdate(any())).thenReturn(Mono.just(admin));
            when(transactionPort.inTransaction(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(usersService.promoteUserToAdminRole("jan@example.com"))
                    .assertNext(dto -> assertThat(dto.getUsername()).isEqualTo("jan@example.com"))
                    .verifyComplete();

            verify(userPort).deleteById(userJan.getId());
            verify(adminPort).addOrUpdate(any());
        }

        @Test
        @DisplayName("User not found: UserServiceException with username")
        void shouldThrowWhenUserNotFound() {
            when(userPort.findByUsername("ghost")).thenReturn(Mono.empty());
            when(transactionPort.inTransaction(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(usersService.promoteUserToAdminRole("ghost"))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(UserServiceException.class);
                        assertThat(ex.getMessage()).contains("ghost");
                    })
                    .verify();

            verifyNoInteractions(adminPort);
        }

        @Test
        @DisplayName("Transaction: pipeline wrapped exactly once")
        void shouldWrapInTransaction() {
            Admin admin = new Admin("jan@example.com", "hashed-pass");
            when(userPort.findByUsername("jan@example.com")).thenReturn(Mono.just(userJan));
            when(userPort.deleteById(userJan.getId())).thenReturn(Mono.just(userJan));
            when(adminPort.addOrUpdate(any())).thenReturn(Mono.just(admin));
            when(transactionPort.inTransaction(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

            StepVerifier.create(usersService.promoteUserToAdminRole("jan@example.com"))
                    .expectNextCount(1)
                    .verifyComplete();

            verify(transactionPort, times(1)).inTransaction(any(Mono.class));
        }
    }
}
