package com.app.infrastructure.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Aspect
@Slf4j
@Component
public class LoggerAspect {

    @Around("@annotation(com.app.infrastructure.aspect.annotations.Loggable)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        var result = joinPoint.proceed();

        if (result instanceof Mono<?> monoResult) {
            var startTime = new AtomicLong();
            return monoResult
                    .doOnSubscribe(subscription -> startTime.set(System.currentTimeMillis()))
                    .doOnSuccess(o -> {
                        if (Objects.nonNull(o) && o instanceof ServerResponse resp) {
                            log.info("Invoking method: {}", Arrays.toString(joinPoint.getArgs()));
                            log.info("Response code: {}", resp.rawStatusCode());
                            log.info("Execution time: {} ms", System.currentTimeMillis() - startTime.get());
                        }
                    });
        }

        if (result instanceof Flux<?> fluxResult) {
            var startTime = new AtomicLong();
            return fluxResult
                    .doOnSubscribe(subscription -> startTime.set(System.currentTimeMillis()))
                    .doOnComplete(() -> {
                        log.info("Invoking method: {}", Arrays.toString(joinPoint.getArgs()));
                        log.info("Execution time: {} ms", System.currentTimeMillis() - startTime.get());
                    });
        }

        return Mono.empty();
    }
}
