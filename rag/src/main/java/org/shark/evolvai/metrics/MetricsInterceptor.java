package com.santander.mep.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MetricsInterceptor implements HandlerInterceptor {

    private final MeterRegistry meterRegistry;

    public MetricsInterceptor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String status = Integer.toString(response.getStatus());

        // Métrica por URI y método
        meterRegistry.counter("http.server.requests.count", Tags.of("method", method, "uri", uri, "status", status)).increment();

        // Métrica global por status
        meterRegistry.counter("http.responses.by.status", Tags.of("status", status)).increment();
    }
}

