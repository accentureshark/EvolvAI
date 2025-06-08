package org.shark.evolvai.embedding.domain.service;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.stream.Collectors;


public class TextChunkingService {

    private static final Logger log = LoggerFactory.getLogger(TextChunkingService.class);


    private final int chunkSize;
    private final int overlap;
    private final List<String> enrichKeys;

    public TextChunkingService(int chunkSize, int overlap, List<String> enrichKeys) {
        this.chunkSize = chunkSize;
        this.overlap = overlap;
        this.enrichKeys = enrichKeys;
        log.info("TextChunkingService inicializado con chunkSize={} overlap={} enrichKeys={}", chunkSize, overlap, enrichKeys);
    }

    public List<TextSegment> chunk(String text, String documentId) {
        return chunk(text, Map.of("documentId", documentId));
    }

    public List<TextSegment> chunk(String text, Map<String, String> baseMetadata) {
        if (text == null || text.trim().isBlank()) {
            log.warn("Texto vacío o en blanco recibido para chunking.");
            return new ArrayList<>();
        }

        log.info("Iniciando chunking de texto de longitud {}: '{}'", text.length(), text.replaceAll("\\s+", " ").trim());

        List<String> words = Arrays.stream(text.split("\\s+"))
                .filter(w -> !w.isBlank())
                .collect(Collectors.toList());

        if (words.isEmpty()) {
            log.warn("No hay palabras útiles para chunking.");
            return new ArrayList<>();
        }

        List<TextSegment> segments = new ArrayList<>();
        int start = 0;
        int chunkCount = 0;

        // Forzar un solo chunk si el texto es corto
        boolean forceOneChunk = true;

        while (start < words.size() || forceOneChunk) {
            forceOneChunk = false;

            int end = Math.min(start + chunkSize, words.size());
            String chunkText = String.join(" ", words.subList(start, end)).trim();

            if (!chunkText.isBlank()) {
                Map<String, String> metadataMap = new HashMap<>(baseMetadata);
                metadataMap.put("chunkIndex", String.valueOf(chunkCount));
                metadataMap.put("section", "fragment-" + chunkCount);

                Metadata metadata = Metadata.from(metadataMap);
                segments.add(new TextSegment(chunkText, metadata));

                log.debug("Fragmento #{} generado: '{}'", chunkCount, chunkText);
            } else {
                log.warn("Fragmento ignorado por estar vacío o en blanco (chunkIndex={})", chunkCount);
            }

            chunkCount++;
            int nextStart = overlap > 0 ? end - overlap : end;
            if (nextStart <= start) break;
            start = nextStart;
        }

        log.info("Chunking finalizado: {} fragmentos generados.", segments.size());
        return segments;
    }
}

