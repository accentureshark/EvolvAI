package org.shark.evolvai.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoint de monitoreo unificado para métricas de los endpoints core.
 * Devuelve la cantidad de llamadas y el tiempo total de cada controller principal.
 */
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoringSummaryController {

    private final MeterRegistry meterRegistry;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("embedding", getStatsFromMeters("api.embedding.documents"));
        summary.put("inference", getStatsFromMeters("api.inference.query"));
        summary.put("llm", getStatsFromMeters("api.llm.getPrompt"));
        return ResponseEntity.ok(summary);
    }

    /**
     * Recorre todos los meters del registry y filtra por nombre y tag "endpoint".
     * Esto es compatible con todos los backends de Micrometer y asegura que no importa
     * cómo se registró el timer, siempre encuentra los datos correctos.
     */
    private Map<String, Object> getStatsFromMeters(String endpointName) {
        Map<String, Object> stats = new HashMap<>();
        double count = 0, total = 0;
        for (io.micrometer.core.instrument.Meter meter : meterRegistry.getMeters()) {
            io.micrometer.core.instrument.Meter.Id id = meter.getId();
            if ("http.endpoint.duration".equals(id.getName()) && endpointName.equals(id.getTag("endpoint"))) {
                for (io.micrometer.core.instrument.Measurement m : meter.measure()) {
                    if (m.getStatistic().name().equalsIgnoreCase("COUNT")) count = m.getValue();
                    if (m.getStatistic().name().equalsIgnoreCase("TOTAL_TIME")) total = m.getValue();
                }
            }
        }
        double mean = count > 0 ? total / count : 0;
        stats.put("calls", (long) count);
        stats.put("total_milliseconds", total + " ms");
        stats.put("mean_milliseconds", mean + " ms");
        return stats;
    }


    /**
     * Endpoint para debug: muestra todos los tags de endpoint registrados actualmente.
     */
    @GetMapping("/debug")
    public ResponseEntity<List<String>> debugEndpoints() {
        List<String> endpoints = meterRegistry.getMeters()
                .stream()
                .filter(m -> "http.endpoint.duration".equals(m.getId().getName()))
                .map(m -> m.getId().getTag("endpoint"))
                .distinct()
                .toList();
        return ResponseEntity.ok(endpoints);
    }
}
