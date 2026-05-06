package com.rzodeczko.infrastructure.persistence.initscripts;

import com.rzodeczko.application.port.out.AdminPort;
import com.rzodeczko.domain.security.Admin;
import com.rzodeczko.infrastructure.persistence.initscripts.subscriber.AdminSubscriber;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

@ChangeUnit(id = "createAdmin", order = "001", author = "CoderNoOne")
@Slf4j
public class InitScripts {

    @Execution
    public void createAdmin(AdminPort adminPort, InitParams initParams, PasswordEncoder passwordEncoder) {

        log.info("Executing script for adding default admin user");

        adminPort
                .addOrUpdate(new Admin(initParams.getAdminUsername(), passwordEncoder.encode(initParams.getPassword())))
                .subscribeWith(new AdminSubscriber());
    }

    @RollbackExecution
    public void rollback() {
        log.info("Rollback script for removing default admin user");
    }

}



