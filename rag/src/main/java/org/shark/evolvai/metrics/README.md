# Monitoreo y Métricas — Proyecto evolvAI

Este módulo expone y recolecta métricas de los endpoints críticos usando [Micrometer](https://micrometer.io/) y Spring Boot Actuator, con soporte para Prometheus y Dynatrace.

---

## Endpoints de Métricas

* **Prometheus:**
  Acceso a todas las métricas:

  ```
  http://localhost:8081/actuator/prometheus
  ```
* **Micrometer/Actuator:**
  Lista de métricas:

  ```
  http://localhost:8081/actuator/metrics
  ```

  Detalle de métrica:

  ```
  http://localhost:8081/actuator/metrics/http.endpoint.duration
  ```

---

## Métricas Custom por Controller

Se instrumentan con la annotation `@MonitoredEndpoint` en los siguientes controllers principales:

* **EmbeddingController** `/api/embeddings`
* **InferenceController** `/api/inference`
* **LlmPromptController** `/api/llm`

### Ejemplo de métricas expuestas

```text
# HELP http_endpoint_duration_seconds
# TYPE http_endpoint_duration_seconds summary
http_endpoint_duration_seconds_count{endpoint="api.embedding.documents"} 3
http_endpoint_duration_seconds_count{endpoint="api.inference.query"} 7
http_endpoint_duration_seconds_count{endpoint="api.llm.getPrompt"} 5
# ...
```

* **endpoint:** Tag con el nombre lógico del endpoint instrumentado (definido en cada controller con `@MonitoredEndpoint(name = "...")`).
* **Unidad:** Las duraciones están ahora registradas y reportadas en **milisegundos** para máxima precisión (antes era en segundos, lo cual truncaba los valores rápidos a 0).

### Otras métricas expuestas

* **Errores por endpoint:**
  `http_endpoint_errors_total{endpoint="...",exception="..."}`
* **Contadores de requests HTTP:**
  `http_server_requests_seconds_count{method="POST",uri="/api/inference/query",...}`

---

## Controles de monitoreo recomendados

1. **Verifica la recolección de métricas:**

    * Ingresa a `/actuator/metrics` y buscá nombres como `http.endpoint.duration` y `http.endpoint.errors`.
2. **Verifica la exportación:**

    * Chequeá los logs para asegurarte que Micrometer intenta exportar a Dynatrace (aunque esté caído, verás `Failed metric ingestion`).
    * Si usás Prometheus, podés scrapear `/actuator/prometheus`.
3. **Forzá requests y errores:**

    * Usá Postman/curl sobre los endpoints principales y chequeá que los contadores suban.
    * Forzá un error y verificá que la métrica de errores sube.

---

## Endpoint de monitoreo unificado

Existe un endpoint de monitoreo agregado para resumir la actividad de los tres controllers principales:

```
GET http://localhost:8081/api/monitoring/summary
```

Devuelve un JSON con las llamadas y tiempos totales/promedio de cada endpoint principal (¡ahora con tiempos reales en milisegundos!):

```json
{
  "embedding": { "calls": 3, "total_seconds": 123, "mean_seconds": 41 },
  "inference": { "calls": 7, "total_seconds": 980, "mean_seconds": 140 },
  "llm": { "calls": 5, "total_seconds": 50, "mean_seconds": 10 }
}
```

**Nota:** Los valores `total_seconds` y `mean_seconds` están en **milisegundos**. Si un endpoint aún no recibió tráfico, responde ceros.

---

## Métodos clave del MonitoringSummaryController

Asegura que los datos de las métricas se obtienen correctamente, incluso si el MeterRegistry no expone timers por cada tag de endpoint:

```java
@GetMapping("/summary")
public ResponseEntity<Map<String, Object>> getSummary() {
    Map<String, Object> summary = new HashMap<>();
    summary.put("embedding", getStatsFromMeters("api.embedding.documents"));
    summary.put("inference", getStatsFromMeters("api.inference.query"));
    summary.put("llm", getStatsFromMeters("api.llm.getPrompt"));
    return ResponseEntity.ok(summary);
}

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
    stats.put("total_milliseconds", total);
    stats.put("mean_milliseconds", mean);
    return stats;
}
```

### Endpoint extra de debug

Permite ver qué endpoints están instrumentados actualmente:

```java
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
```

---

## Troubleshooting

* Si no ves las métricas custom:

    * Asegurate de que hay tráfico hacia los endpoints (`curl` o Postman).
    * Revisá la annotation `@MonitoredEndpoint` esté bien aplicada y la config de Actuator exponga `metrics` y `prometheus`.
* Si Dynatrace no está disponible, la colección y exposición por Prometheus/Actuator funciona igual.

---

## Referencias

* [Micrometer Docs](https://micrometer.io/docs)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/actuator-api/htmlsingle/)
* [Prometheus Docs](https://prometheus.io/docs/introduction/overview/)
