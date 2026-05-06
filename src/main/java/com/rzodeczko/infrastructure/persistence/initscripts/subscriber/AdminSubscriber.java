package com.rzodeczko.infrastructure.persistence.initscripts.subscriber;

import com.rzodeczko.domain.security.Admin;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Slf4j
public class AdminSubscriber implements Subscriber<Admin> {

    @Override
    public void onSubscribe(Subscription subscription) {
        subscription.request(1);
    }

    @Override
    public void onNext(Admin admin) {
        log.info("Admin with username is being saved: %s".formatted(admin.getUsername()));
    }

    @Override
    public void onError(Throwable throwable) {
        log.info("Error occurred during saving admin");
        log.error(throwable.getMessage(), throwable);
    }

    @Override
    public void onComplete() {
        log.info("Admin has been successfully saved");
    }
}
