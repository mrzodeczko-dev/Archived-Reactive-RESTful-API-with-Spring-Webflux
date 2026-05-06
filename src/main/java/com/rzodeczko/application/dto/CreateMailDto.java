package com.rzodeczko.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CreateMailDto {

    private String to;
    private String htmlContent;
    private String title;

    public MailDto toMailDto() {
        return MailDto.builder()
                .title(title)
                .to(to)
                .build();
    }
}
