package org.shark.evolvai.util;

import dev.langchain4j.data.document.Metadata;
import org.shark.evolvai.inference.model.DataSourceInfo;

import java.util.HashMap;
import java.util.Map;

public class MetadataBuilder {

    /**
     * Construye una metadata homogénea desde un DataSourceInfo.
     */
    public static Metadata fromDataSourceInfo(DataSourceInfo source) {
        Map<String, Object> base = new HashMap<>();
        base.put("sourceType", source.getType().name());
        if (source.getId() != null && !source.getId().isBlank()) {
            base.put("sourceId", source.getId());
        }
        if (source.getParams() != null) {
            base.putAll(source.getParams());
        }
        return Metadata.from(base);
    }

    /**
     * Convierte DataSourceInfo en un map plano de metadatos.
     */
    public static Map<String, Object> toMap(DataSourceInfo source) {
        Map<String, Object> map = new HashMap<>();
        map.put("sourceType", source.getType().name());
        if (source.getId() != null && !source.getId().isBlank()) {
            map.put("sourceId", source.getId());
        }
        if (source.getParams() != null) {
            map.putAll(source.getParams());
        }
        return map;
    }

    /**
     * Enriquecer una metadata base con campos adicionales.
     */
    public static Metadata enrich(Metadata base, Map<String, Object> extras) {
        Map<String, Object> combined = new HashMap<>(base.toMap());
        if (extras != null) {
            combined.putAll(extras);
        }
        return Metadata.from(combined);
    }
}
