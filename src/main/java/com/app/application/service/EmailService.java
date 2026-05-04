package com.app.application.service;

import com.app.application.dto.CreateMailDto;
import com.app.application.dto.CreateMailsDto;
import com.app.application.dto.MailDto;
import com.app.application.exception.EmailServiceException;
import com.app.application.validator.CreateMailDtoValidator;
import com.app.application.validator.CreateMailsDtoValidator;
import com.app.application.validator.util.Validations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final CreateMailDtoValidator createMailDtoValidator;
    private final CreateMailsDtoValidator createMailsDtoValidator;

    public Mono<MailDto> sendSingleEmail(CreateMailDto createMailDto) {

        var errors = createMailDtoValidator.validate(createMailDto);

        if (Validations.hasErrors(errors)) {
            return Mono.error(() -> new EmailServiceException("Mail is not valid. Errors are: [%s]".formatted(Validations.createErrorMessage(errors))));
        }

        return Mono.fromCallable(() -> sendAsHtml(createMailDto))
                .subscribeOn(Schedulers.boundedElastic())
                .map(CreateMailDto::toMailDto);
    }

    public Flux<MailDto> sendMultipleEmails(CreateMailsDto createMailDtoList) {

        var errors = createMailsDtoValidator.validate(createMailDtoList);

        if (Validations.hasErrors(errors)) {
            return Flux.error(() -> new EmailServiceException("Some mails are not valid. Errors are: [%s]".formatted(Validations.createErrorMessage(errors))));
        }

        return Mono.fromCallable(() -> sendAsHtml(createMailDtoList.getMails()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(CreateMailDto::toMailDto);
    }

    private MimeMessage createMimeMessage(CreateMailDto createMailDto) {

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, false);
            messageHelper.setText(createMailDto.getHtmlContent(), true);
            messageHelper.setTo(createMailDto.getTo());
            messageHelper.setSubject(createMailDto.getTitle());
            return mimeMessage;
        } catch (MessagingException e) {
            log.info("Exception during message generation");
            log.error(e.getMessage(), e);
            throw new EmailServiceException(e.getMessage());
        }
    }

    private CreateMailDto sendAsHtml(CreateMailDto createMailDto) {
        mailSender.send(createMimeMessage(createMailDto));
        return createMailDto;
    }

    private List<CreateMailDto> sendAsHtml(List<CreateMailDto> createMailDtos) {

        sendBulk(createMailDtos
                .stream()
                .map(this::createMimeMessage)
                .toArray(MimeMessage[]::new));

        return createMailDtos;
    }

    private void sendBulk(MimeMessage... messages) {
        mailSender.send(messages);
    }
}
