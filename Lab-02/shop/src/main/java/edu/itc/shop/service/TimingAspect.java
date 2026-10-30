package edu.itc.shop.service;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TimingAspect {

    private static final Logger log = LoggerFactory.getLogger(TimingAspect.class);

    @Around("execution(public * edu.itc.shop.service..*(..))")
    public Object timeMethod(ProceedingJoinPoint pjp) throws Throwable {
        long startNanos = System.nanoTime();
        try {
            return pjp.proceed();
        } finally {
            long tookMicros = (System.nanoTime() - startNanos) / 1_000;
            log.debug("{} took {} µs", pjp.getSignature().toShortString(), tookMicros);
        }
    }
}