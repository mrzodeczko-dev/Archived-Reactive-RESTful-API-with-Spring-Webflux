package com.rzodeczko.application.service;

import com.rzodeczko.application.dto.CreateMailDto;
import com.rzodeczko.application.dto.CreateMailsDto;
import com.rzodeczko.application.dto.MailDto;
import com.rzodeczko.application.exception.EmailServiceException;
import com.rzodeczko.application.port.out.MailPort;
import com.rzodeczko.application.validator.CreateMailDtoValidator;
import com.rzodeczko.application.validator.CreateMailsDtoValidator;
import com.rzodeczko.application.validator.util.Validations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;

public class EmailService {

    private static final Logger log = LogManager.getLogger(EmailService.class);
    private static final int MAX_RETRY_ATTEMPTS = 2;
    private static final Duration RETRY_BACKOFF = Duration.ofSeconds(2);

    private final MailPort mailPort;
    private final CreateMailDtoValidator createMailDtoValidator;
    private final CreateMailsDtoValidator createMailsDtoValidator;

    public EmailService(MailPort mailPort,
                        CreateMailDtoValidator createMailDtoValidator,
                        CreateMailsDtoValidator createMailsDtoValidator) {
        this.mailPort = mailPort;
        this.createMailDtoValidator = createMailDtoValidator;
        this.createMailsDtoValidator = createMailsDtoValidator;
    }

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

        return Mono.fromRunnable(() -> mailPort.sendBulk(createMailDtoList.getMails()))
                .subscribeOn(Schedulers.boundedElastic())
                .retryWhen(retrySpec())
                .thenMany(Flux.fromIterable(createMailDtoList.getMails()))
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
