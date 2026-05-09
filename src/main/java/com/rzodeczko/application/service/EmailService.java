package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.CreateMailDto;
import com.rzodeczko.application.dto.CreateMailsDto;
import com.rzodeczko.application.dto.MailDto;
import com.rzodeczko.application.dto.SendEmailToSelfDto;
import com.rzodeczko.application.exception.EmailServiceException;
import com.rzodeczko.application.port.out.MailPort;
import com.rzodeczko.application.validator.CreateMailDtoValidator;
import com.rzodeczko.application.validator.CreateMailsDtoValidator;
import com.rzodeczko.application.validator.SendEmailToSelfDtoValidator;
import com.rzodeczko.application.validator.util.Validations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;

public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final int MAX_RETRY_ATTEMPTS = 2;
    private static final Duration RETRY_BACKOFF = Duration.ofSeconds(2);

    private final MailPort mailPort;
    private final CreateMailDtoValidator createMailDtoValidator;
    private final CreateMailsDtoValidator createMailsDtoValidator;
    private final SendEmailToSelfDtoValidator sendEmailToSelfDtoValidator;

    public EmailService(MailPort mailPort,
                        CreateMailDtoValidator createMailDtoValidator,
                        CreateMailsDtoValidator createMailsDtoValidator,
                        SendEmailToSelfDtoValidator sendEmailToSelfDtoValidator) {
        this.mailPort = mailPort;
        this.createMailDtoValidator = createMailDtoValidator;
        this.createMailsDtoValidator = createMailsDtoValidator;
        this.sendEmailToSelfDtoValidator = sendEmailToSelfDtoValidator;
    }

    /**
     * Sends an email to a recipient resolved by the caller (typically the authenticated user's
     * own address). This is the entry point exposed via {@code POST /emails/send/single} — the
     * caller is responsible for providing a trusted {@code resolvedTo}, the request body is not
     * trusted to specify it.
     */
    public Mono<MailDto> sendEmailToSelf(SendEmailToSelfDto dto, String resolvedTo) {
        var errors = sendEmailToSelfDtoValidator.validate(dto);
        if (Validations.hasErrors(errors)) {
            return Mono.error(() -> new EmailServiceException("Mail is not valid. Errors are: [%s]"
                    .formatted(Validations.createErrorMessage(errors))));
        }
        if (resolvedTo == null || resolvedTo.isBlank()) {
            return Mono.error(() -> new EmailServiceException("Recipient email is missing"));
        }
        return sendSingleEmail(dto.toCreateMailDto(resolvedTo));
    }

    /**
     * Lower-level send. Kept package/api-internal in spirit — used by {@link #sendEmailToSelf}
     * and by bulk admin flows that already validate recipients elsewhere.
     */
    public Mono<MailDto> sendSingleEmail(CreateMailDto createMailDto) {
        var errors = createMailDtoValidator.validate(createMailDto);

        if (Validations.hasErrors(errors)) {
            return Mono.error(() -> new EmailServiceException("Mail is not valid. Errors are: [%s]"
                    .formatted(Validations.createErrorMessage(errors))));
        }

        return Mono.fromRunnable(() -> mailPort.send(createMailDto))
                .subscribeOn(Schedulers.boundedElastic())
                .retryWhen(retrySpec())
                .thenReturn(createMailDto.toMailDto());
    }

    public Flux<MailDto> sendMultipleEmails(CreateMailsDto createMailDtoList) {

        var errors = createMailsDtoValidator.validate(createMailDtoList);

        if (Validations.hasErrors(errors)) {
            return Flux.error(() -> new EmailServiceException("Some mails are not valid. Errors are: [%s]"
                    .formatted(Validations.createErrorMessage(errors))));
        }

        return Mono.fromRunnable(() -> mailPort.sendBulk(createMailDtoList.mails()))
                .subscribeOn(Schedulers.boundedElastic())
                .retryWhen(retrySpec())
                .thenMany(Flux.fromIterable(createMailDtoList.mails()))
                .map(CreateMailDto::toMailDto);
    }

    private Retry retrySpec() {
        return Retry.backoff(MAX_RETRY_ATTEMPTS, RETRY_BACKOFF)
                .doBeforeRetry(signal -> log.warn(
                        "Retrying mail send, attempt [{}], reason: {}",
                        signal.totalRetries() + 1,
                        signal.failure().getMessage()))
                .onRetryExhaustedThrow((spec, signal) -> new EmailServiceException(
                        "Mail sending failed after [%d] attempts. Last error: [%s]"
                                .formatted(MAX_RETRY_ATTEMPTS, signal.failure().getMessage())));
    }
}