package org.shark.evolvai.embedding.domain.service;
import java.util.Map;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TextChunkingService {

    private static final Logger log = LoggerFactory.getLogger(TextChunkingService.class);

    private final int chunkSize;
    private final int overlap;

    public TextChunkingService(int chunkSize, int overlap) {
        this.chunkSize = chunkSize;
        this.overlap = overlap;
        log.info("TextChunkingService inicializado con chunkSize={} y overlap={}", chunkSize, overlap);
    }

    public List<TextSegment> chunk(String text) {
        log.info("Iniciando chunking de texto de longitud {}...", text.length());
        List<TextSegment> segments = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            log.warn("Texto vacío recibido para chunking.");
            return segments;
        }

        String[] words = text.split("\\s+");
        int start = 0;
        int chunkCount = 0;

        while (start < words.length) {
            int end = Math.min(start + chunkSize, words.length);
            StringBuilder chunk = new StringBuilder();
            for (int i = start; i < end; i++) {
                chunk.append(words[i]).append(" ");
            }
            String chunkText = chunk.toString().trim();
            segments.add(new TextSegment(chunkText, Metadata.from(Map.of())));
            log.debug("Fragmento #{} generado: {} palabras, '{}'...", chunkCount, (end - start), chunkText.substring(0, Math.min(40, chunkText.length())));
            chunkCount++;
            if (end == words.length) break;
            start = end - overlap;
            if (start < 0) start = 0;
        }
        log.info("Chunking finalizado: {} fragmentos generados.", segments.size());
        return segments;
    }
}