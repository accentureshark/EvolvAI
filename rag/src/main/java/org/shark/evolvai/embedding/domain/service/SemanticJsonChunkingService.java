package org.shark.evolvai.embedding.domain.service;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class SemanticJsonChunkingService {

    /**
     * Divide una lista de entradas JSON (cada una con "texto") en TextSegments.
     * Usa siempre el documentId que viene de baseMetadata.
     */
    public List<TextSegment> chunk(List<Map<String, Object>> data, Map<String, String> baseMetadata) {
        List<TextSegment> segments = new ArrayList<>();
        int chunkIndex = 0;

        for (Map<String, Object> entry : data) {
            Object rawText = entry.get("texto");
            if (rawText == null) {
                log.warn("Entrada ignorada: no contiene campo 'texto': {}", entry);
                continue;
            }

            String text = rawText.toString().trim();
            if (text.isBlank()) {
                log.warn("Entrada ignorada: texto en blanco (chunkIndex={})", chunkIndex);
                continue;
            }

            // Clona la metadata base y suma los datos de chunk/contexto
            Map<String, String> metadata = new HashMap<>(baseMetadata);
            metadata.put("chunkIndex", String.valueOf(chunkIndex));

            // (Opcional: podés sumar más datos si tu entry tiene más campos relevantes)
            // Ejemplo: si querés meter 'area' o 'nivel'
            entry.forEach((k, v) -> {
                if (v != null && !"texto".equals(k)) {
                    metadata.put(k, v.toString());
                }
            });

            // ¡NO toques el documentId! Solo loguealo para auditar
            log.debug("Creando chunk index={} documentId={}", chunkIndex, metadata.get("documentId"));

            segments.add(new TextSegment(text, Metadata.from(metadata)));
            chunkIndex++;
        }
        return segments;
    }
}
