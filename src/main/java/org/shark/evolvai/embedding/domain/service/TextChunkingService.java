package org.shark.evolvai.embedding.domain.service;

import java.util.Map;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class TextChunkingService {

    private static final Logger log = LoggerFactory.getLogger(TextChunkingService.class);

    private final int chunkSize;
    private final int overlap;

    public TextChunkingService(int chunkSize, int overlap) {
        this.chunkSize = chunkSize;
        this.overlap = overlap;
        log.info("TextChunkingService inicializado con chunkSize={} y overlap={}", chunkSize, overlap);
    }

    public List<TextSegment> chunk(String text, String documentId) {
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
            int startChar = -1;
            int endChar = -1;
            int charCount = 0;

            for (int i = 0; i < words.length; i++) {
                if (i == start) {
                    startChar = charCount;
                }
                if (i == end - 1) {
                    endChar = charCount + words[i].length();
                }
                charCount += words[i].length() + 1; // espacio
            }

            for (int i = start; i < end; i++) {
                chunk.append(words[i]).append(" ");
            }

            String chunkText = chunk.toString().trim();
            Metadata metadata = Metadata.from(Map.of(
                    "documentId", documentId,
                    "chunkIndex", String.valueOf(chunkCount),
                    "source", documentId,
                    "section", "fragment-" + chunkCount,
                    "charStart", String.valueOf(startChar),
                    "charEnd", String.valueOf(endChar)
            ));

            segments.add(new TextSegment(chunkText, metadata));
            log.debug("Fragmento #{} generado: {} palabras", chunkCount, (end - start));
            chunkCount++;

            if (end == words.length) break;
            start = end - overlap;
            if (start < 0) start = 0;
        }

        log.info("Chunking finalizado: {} fragmentos generados.", segments.size());
        return segments;
    }


}
