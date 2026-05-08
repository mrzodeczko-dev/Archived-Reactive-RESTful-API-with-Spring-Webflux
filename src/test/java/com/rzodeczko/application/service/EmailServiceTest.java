package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.CreateMailDto;
import com.rzodeczko.application.dto.CreateMailsDto;
import com.rzodeczko.application.exception.EmailServiceException;
import com.rzodeczko.application.port.out.MailPort;
import com.rzodeczko.application.validator.CreateMailDtoValidator;
import com.rzodeczko.application.validator.CreateMailsDtoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private MailPort mailPort;
    @Mock
    private CreateMailDtoValidator createMailDtoValidator;
    @Mock
    private CreateMailsDtoValidator createMailsDtoValidator;

    @InjectMocks
    private EmailService emailService;

    private CreateMailDto validMail;

    @BeforeEach
    void setUp() {
        validMail = CreateMailDto.builder()
                .to("user@example.com")
                .title("Hello")
                .htmlContent("<p>hi</p>")
                .build();
    }

    @Test
    @DisplayName("sendSingleEmail — emits validation error when validator finds issues")
    void sendSingleEmail_whenValidatorFails_shouldEmitError() {
        Map<String, String> errors = new HashMap<>();
        errors.put("To email", "is not valid");
        when(createMailDtoValidator.validate(validMail)).thenReturn(errors);

        StepVerifier.create(emailService.sendSingleEmail(validMail))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(EmailServiceException.class)
                        .hasMessageContaining("Mail is not valid"))
                .verify();

        verify(mailPort, never()).send(any(CreateMailDto.class));
    }

    @Test
    @DisplayName("sendSingleEmail — happy path returns MailDto and sends mime message")
    void sendSingleEmail_whenValid_shouldReturnMailDto() {
        when(createMailDtoValidator.validate(validMail)).thenReturn(new HashMap<>());

        StepVerifier.create(emailService.sendSingleEmail(validMail))
                .assertNext(dto -> {
                    assertThat(dto.to()).isEqualTo("user@example.com");
                    assertThat(dto.title()).isEqualTo("Hello");
                })
                .verifyComplete();

        verify(mailPort, times(1)).send(any(CreateMailDto.class));
    }

    @Test
    @DisplayName("sendMultipleEmails — emits validation error when validator finds issues")
    void sendMultipleEmails_whenValidatorFails_shouldEmitError() {
        CreateMailsDto bulk = CreateMailsDto.builder().mails(List.of(validMail)).build();
        Map<String, Object> errors = new HashMap<>();
        errors.put("0", List.of(Map.entry("To email", "is not valid")));
        when(createMailsDtoValidator.validate(bulk)).thenReturn(errors);

        StepVerifier.create(emailService.sendMultipleEmails(bulk))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(EmailServiceException.class)
                        .hasMessageContaining("Some mails are not valid"))
                .verify();

        // No bulk send should have happened.
        verify(mailPort, never()).send(any(CreateMailDto.class));
    }

    @Test
    @DisplayName("sendMultipleEmails — happy path emits one MailDto per input and sends bulk")
    void sendMultipleEmails_whenValid_shouldEmitMailDtos() {
        CreateMailDto another = CreateMailDto.builder()
                .to("user2@example.com").title("T2").htmlContent("<p>2</p>").build();
        CreateMailsDto bulk = CreateMailsDto.builder().mails(List.of(validMail, another)).build();

        when(createMailsDtoValidator.validate(bulk)).thenReturn(new HashMap<>());

        StepVerifier.create(emailService.sendMultipleEmails(bulk))
                .assertNext(dto -> assertThat(dto.to()).isEqualTo("user@example.com"))
                .assertNext(dto -> assertThat(dto.to()).isEqualTo("user2@example.com"))
                .verifyComplete();

        verify(mailPort, times(1)).sendBulk(anyList());
    }
}
