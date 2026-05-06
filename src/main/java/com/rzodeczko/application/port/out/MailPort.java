package com.rzodeczko.application.port.out;

import com.rzodeczko.application.dto.CreateMailDto;

import java.util.List;

public interface MailPort {
    void send(CreateMailDto mail);
    void sendBulk(List<CreateMailDto> mails);
}
