package com.rzodeczko.infrastructure.mail;

import com.rzodeczko.application.dto.CreateMailDto;
import com.rzodeczko.application.exception.EmailServiceException;
import com.rzodeczko.application.port.out.MailPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class JavaMailSenderAdapter implements MailPort {

    private final JavaMailSender mailSender;

    public JavaMailSenderAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(CreateMailDto mail) {
        mailSender.send(createMimeMessage(mail));
    }

    @Override
    public void sendBulk(List<CreateMailDto> mails) {
        MimeMessage[] messages = mails.stream()
                .map(this::createMimeMessage)
                .toArray(MimeMessage[]::new);
        mailSender.send(messages);
    }

    private MimeMessage createMimeMessage(CreateMailDto mail) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false);
            helper.setText(mail.htmlContent(), true);
            helper.setTo(mail.to());
            helper.setSubject(mail.title());
            return mimeMessage;
        } catch (MessagingException e) {
            log.error(e.getMessage(), e);
            throw new EmailServiceException(e.getMessage());
        }
    }
}
