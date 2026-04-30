package com.app.infrastructure.mongo.initscripts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitParamsImpl implements InitParams {

    @Value("${adminusername}")
    private String username;
    @Value("${adminpassword}")
    private String pass;

    @Override
    public String getAdminUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return pass;
    }
}
