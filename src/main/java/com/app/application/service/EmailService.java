package com.app.application.service;

import com.app.application.dto.CreateMailDto;
import com.app.application.dto.CreateMailsDto;
import com.app.application.dto.MailDto;
import com.app.application.exception.EmailServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public Mono<MailDto> sendSingleEmail(CreateMailDto createMailDto) {
        if (createMailDto == null) {
            return Mono.error(() -> new EmailServiceException("Mail data is null"));
        }
        return Mono.fromCallable(() -> {
                    var message = new SimpleMailMessage();
                    message.setTo(createMailDto.getTo());
                    message.setSubject(createMailDto.getSubject());
                    message.setText(createMailDto.getText());
                    mailSender.send(message);
                    return MailDto.builder()
                            .to(createMailDto.getTo())
                            .subject(createMailDto.getSubject())
                            .text(createMailDto.getText())
                            .build();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(e -> new EmailServiceException("Failed to send email: " + e.getMessage()));
    }

    public Flux<MailDto> sendMultipleEmails(CreateMailsDto createMailsDto) {
        if (createMailsDto == null || createMailsDto.getMails() == null) {
            return Flux.error(() -> new EmailServiceException("Mails data is null"));
        }
        return Flux.fromIterable(createMailsDto.getMails())
                .flatMap(this::sendSingleEmail);
    }
}
