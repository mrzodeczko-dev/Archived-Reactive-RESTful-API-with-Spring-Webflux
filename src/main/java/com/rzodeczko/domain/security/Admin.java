package com.rzodeczko.domain.security;

import com.rzodeczko.domain.security.enums.Role;

public final class Admin extends BaseUser {

    public Admin() {
    }

    public Admin(String username, String password) {
        super(username, password, Role.ROLE_ADMIN);
    }
}
