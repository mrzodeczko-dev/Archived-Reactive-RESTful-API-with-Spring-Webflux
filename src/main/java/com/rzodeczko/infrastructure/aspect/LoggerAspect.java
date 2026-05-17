package com.rzodeczko.infrastructure.aspect;

import com.rzodeczko.application.dto.CreateUserDto;
import com.rzodeczko.infrastructure.security.dto.AuthenticationDto;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.CorePublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Aspect
@Slf4j
@Component
public class LoggerAspect {

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")  // IntelliJ false positive
    @Around("@annotation(com.rzodeczko.infrastructure.aspect.annotations.Loggable)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        return switch (joinPoint.proceed()) {
            case Mono<?> mono -> logMono(joinPoint, mono);
            case Flux<?> flux -> logFlux(joinPoint, flux);
            default -> joinPoint.proceed();
        };
    }

    private Mono<?> logMono(ProceedingJoinPoint joinPoint, Mono<?> mono) {
        long start = System.currentTimeMillis();

        logStart(joinPoint);

        return mono
                .doOnSuccess(value -> {
                    if (value instanceof ServerResponse response) {
                        log.info("Response code: {}", response.statusCode().value());
                    }
                })
                .doOnError(error ->
                        log.error("Method failed: {}", joinPoint.getSignature().toShortString(), error))
                .doFinally(signalType ->
                        logExecution(joinPoint, start, signalType.name()));
    }

    private Flux<?> logFlux(ProceedingJoinPoint joinPoint, Flux<?> flux) {
        long start = System.currentTimeMillis();

        logStart(joinPoint);

        return flux
                .doOnError(error ->
                        log.error("Method failed: {}", joinPoint.getSignature().toShortString(), error))
                .doFinally(signalType ->
                        logExecution(joinPoint, start, signalType.name()));
    }

    private void logStart(ProceedingJoinPoint joinPoint) {
        log.info("Invoking method: {}", joinPoint.getSignature().toShortString());
        log.info("Arguments: {}", Arrays.toString(safeArgs(joinPoint.getArgs())));
    }

    private void logExecution(ProceedingJoinPoint joinPoint, long start, String signalType) {
        log.info("Finished method: {}", joinPoint.getSignature().toShortString());
        log.info("Signal: {}", signalType);
        log.info("Execution time: {} ms", System.currentTimeMillis() - start);
    }

    private Object[] safeArgs(Object[] args) {
        return Arrays.stream(args)
                .map(a -> a instanceof CreateUserDto || a instanceof AuthenticationDto ? "[REDACTED]" : String.valueOf(a))
                .toArray();
    }
}