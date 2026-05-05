package com.app.infrastructure.mongo.initscripts;

import com.app.domain.security.Admin;
import com.app.domain.security.AdminRepository;
import com.app.infrastructure.mongo.initscripts.subscriber.AdminSubscriber;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

@ChangeUnit(id = "createAdmin", order = "001", author = "CoderNoOne")
@Slf4j
public class InitScripts {

    @Execution
    public void createAdmin(AdminRepository adminRepository, InitParams initParams, PasswordEncoder passwordEncoder) {

        log.info("Executing script for adding default admin user");

        adminRepository
                .addOrUpdate(new Admin(initParams.getAdminUsername(), passwordEncoder.encode(initParams.getPassword())))
                .subscribeWith(new AdminSubscriber());
    }

    @RollbackExecution
    public void rollback() {
        log.info("Rollback script for removing default admin user");
    }

}



