package com.rzodeczko.infrastructure.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Aspect
@Slf4j
@Component
public class LoggerAspect {

    @Around("@annotation(com.rzodeczko.infrastructure.aspect.annotations.Loggable)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        if (result instanceof Mono<?> mono) {
            return logMono(joinPoint, mono);
        }

        if (result instanceof Flux<?> flux) {
            return logFlux(joinPoint, flux);
        }

        return result;
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
        log.info("Arguments: {}", Arrays.toString(joinPoint.getArgs()));
    }

    private void logExecution(ProceedingJoinPoint joinPoint, long start, String signalType) {
        log.info("Finished method: {}", joinPoint.getSignature().toShortString());
        log.info("Signal: {}", signalType);
        log.info("Execution time: {} ms", System.currentTimeMillis() - start);
    }
}