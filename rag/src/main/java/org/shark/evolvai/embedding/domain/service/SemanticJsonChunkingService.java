package org.shark.evolvai.embedding.domain.service;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class SemanticJsonChunkingService {

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

            Map<String, String> combinedMetadata = new HashMap<>(baseMetadata);
            combinedMetadata.put("chunkIndex", String.valueOf(chunkIndex));
            combinedMetadata.put("section", "fragment-" + chunkIndex);

            for (Map.Entry<String, Object> meta : entry.entrySet()) {
                if (!"texto".equals(meta.getKey()) && meta.getValue() != null) {
                    combinedMetadata.put(meta.getKey(), meta.getValue().toString());
                }
            }

            Metadata metadata = Metadata.from(combinedMetadata);
            segments.add(new TextSegment(text, metadata));
            log.info("Fragmento generado (chunkIndex={}): '{}'", chunkIndex, text);

            chunkIndex++;
        }

        log.info("Chunking semántico finalizado: {} fragmentos generados.", segments.size());
        return segments;
    }
}