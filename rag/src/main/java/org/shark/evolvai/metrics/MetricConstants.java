package org.shark.evolvai.metrics;


/**
 * Contiene nombres de métricas y claves de tags reutilizables.
 * No contiene valores de negocio específicos para evitar acoplamientos.
 */
public final class MetricConstants {

    // Nombres genéricos de métricas
    public static final String METRIC_ENDPOINT_ERRORS = "http.endpoint.errors";
    public static final String METRIC_ENDPOINT_DURATION = "http.endpoint.duration";
    public static final String METRIC_REQUEST_COUNT = "http.server.requests.count";
    public static final String METRIC_RESPONSE_BY_STATUS = "http.responses.by.status";

    // Tags comunes
    public static final String TAG_METHOD = "method";
    public static final String TAG_URI = "uri";
    public static final String TAG_STATUS = "status";
    public static final String TAG_ENDPOINT = "endpoint";
    public static final String TAG_EXCEPTION = "exception";

    private MetricConstants() {
        // Evita instanciación
    }
}
