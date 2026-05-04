package com.app.application.service;

import com.app.application.dto.CreateMailDto;
import com.app.application.dto.CreateMailsDto;
import com.app.application.exception.EmailServiceException;
import com.app.application.validator.CreateMailDtoValidator;
import com.app.application.validator.CreateMailsDtoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import reactor.test.StepVerifier;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private CreateMailDtoValidator createMailDtoValidator;
    @Mock
    private CreateMailsDtoValidator createMailsDtoValidator;

    @InjectMocks
    private EmailService emailService;

    private CreateMailDto validMail;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        validMail = CreateMailDto.builder()
                .to("user@example.com")
                .title("Hello")
                .htmlContent("<p>hi</p>")
                .build();

        // Create a real MimeMessage with empty session — simpler than mocking every method call
        mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
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

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("sendSingleEmail — happy path returns MailDto and sends mime message")
    void sendSingleEmail_whenValid_shouldReturnMailDto() {
        when(createMailDtoValidator.validate(validMail)).thenReturn(new HashMap<>());
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        StepVerifier.create(emailService.sendSingleEmail(validMail))
                .assertNext(dto -> {
                    assertThat(dto.getTo()).isEqualTo("user@example.com");
                    assertThat(dto.getTitle()).isEqualTo("Hello");
                })
                .verifyComplete();

        verify(mailSender, times(1)).send(any(MimeMessage.class));
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
        verify(mailSender, never()).send(any(MimeMessage.class));
        verify(mailSender, never()).createMimeMessage();
    }

    @Test
    @DisplayName("sendMultipleEmails — happy path emits one MailDto per input and sends bulk")
    void sendMultipleEmails_whenValid_shouldEmitMailDtos() {
        CreateMailDto another = CreateMailDto.builder()
                .to("user2@example.com").title("T2").htmlContent("<p>2</p>").build();
        CreateMailsDto bulk = CreateMailsDto.builder().mails(List.of(validMail, another)).build();

        when(createMailsDtoValidator.validate(bulk)).thenReturn(new HashMap<String, Object>());
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        StepVerifier.create(emailService.sendMultipleEmails(bulk))
                .assertNext(dto -> assertThat(dto.getTo()).isEqualTo("user@example.com"))
                .assertNext(dto -> assertThat(dto.getTo()).isEqualTo("user2@example.com"))
                .verifyComplete();

        // Bulk path uses send(MimeMessage...) varargs — verify createMimeMessage was used twice.
        verify(mailSender, times(2)).createMimeMessage();
    }
}
