package org.shark.evolvai.metrics;



import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@SuppressWarnings({"all"})
public class MonitoredEndpointAspect {

    private final MeterRegistry meterRegistry;

    @Around("@annotation(MonitoredEndpoint)")

    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        MonitoredEndpoint annotation = method.getAnnotation(MonitoredEndpoint.class);

        String className = joinPoint.getTarget().getClass().getSimpleName().replace("Controller", "").toLowerCase();
        String methodName = method.getName();
        String endpointName = annotation.name().isEmpty() ? className + "." + methodName : annotation.name();

        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } catch (Exception ex) {
            meterRegistry.counter("http.endpoint.errors", Tags.of("endpoint", endpointName, "exception", ex.getClass().getSimpleName())).increment();
            throw ex;
        } finally {
            long end = System.nanoTime();
            long durationMillis = (end - start) / 1_000_000;
            meterRegistry.timer("http.endpoint.duration", Tags.of("endpoint", endpointName))
                    .record(durationMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
    }

}
