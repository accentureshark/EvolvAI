package org.shark.evolvai.embedding.domain.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.shark.evolvai.config.RagProperties;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartChunkingService {

    private final TextChunkingService textChunkingService;
    private final SemanticJsonChunkingService semanticJsonChunkingService;
    private final RagProperties ragProperties;

    public List<TextSegment> chunk(Object input, Map<String, String> baseMetadata) {
        if (input instanceof Document document) {
            int size = ragProperties.getEmbedding().getChunking().getSize();
            int overlap = ragProperties.getEmbedding().getChunking().getOverlap();
            DocumentBySentenceSplitter splitter = new DocumentBySentenceSplitter(size, overlap);
            List<TextSegment> chunks = splitter.split(document);
            for (TextSegment chunk : chunks) {
                chunk.metadata().put("documentId", baseMetadata.get("documentId"));
            }
            return chunks;
        }

        if (input instanceof String plainText) {
            log.info(
                "Usando chunking por palabras para texto plano. documentId={}",
                baseMetadata.get("documentId")
            );
            // ¡Asegura que textChunkingService NO pise el documentId!
            return textChunkingService.chunk(plainText, baseMetadata);
        }

        if (input instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map) {
            log.info(
                "Usando chunking semántico para JSON con entradas de texto. documentId={}",
                baseMetadata.get("documentId")
            );
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) list;
            // ¡Asegura que semanticJsonChunkingService NO pise el documentId!
            return semanticJsonChunkingService.chunk(data, baseMetadata);
        }

        log.warn("Entrada no reconocida para chunking: tipo {}", input.getClass().getSimpleName());
        return Collections.emptyList();
    }
}
