package org.shark.evolvai.metrics;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetricUtils {

    private final MeterRegistry meterRegistry;

    /**
     * Incrementa una métrica de negocio con tags dinámicos.
     *
     * @param metricName nombre de la métrica (ej: business.cta.total)
     * @param tags pares clave-valor para los tags
     */
    public void increment(String metricName, String... tags) {
        if (tags.length % 2 != 0) {
            throw new IllegalArgumentException("Los tags deben venir en pares clave-valor");
        }
        meterRegistry.counter(metricName, Tags.of(tags)).increment();
    }
}
