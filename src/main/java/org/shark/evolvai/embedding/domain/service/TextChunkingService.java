package org.shark.evolvai.embedding.domain.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkingService {

    private final int maxTokens;
    private final int overlap;

    public TextChunkingService(int maxTokens, int overlap) {
        this.maxTokens = maxTokens;
        this.overlap = overlap;
    }

    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentChunk = new StringBuilder();

        int currentTokenCount = 0;
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (currentTokenCount + word.length() > maxTokens) {
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder();

                // Agregar superposición
                int start = Math.max(0, i - overlap);
                for (int j = start; j < i; j++) {
                    currentChunk.append(words[j]).append(" ");
                }
                currentTokenCount = currentChunk.length();
            }
            currentChunk.append(word).append(" ");
            currentTokenCount += word.length();
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }
}