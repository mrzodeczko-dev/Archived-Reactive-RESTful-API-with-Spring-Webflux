package com.rzodeczko.application.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum UserField {
    USERNAME("Username"), EMAIL("email");

    @Getter
    private final String desc;
}


