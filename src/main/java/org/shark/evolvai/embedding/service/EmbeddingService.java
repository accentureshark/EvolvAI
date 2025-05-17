package org.shark.evolvai.embedding.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;

import java.util.List;

public interface EmbeddingService {
    Embedding embedText(String text);
    List<Embedding> embedTexts(List<String> texts);
    Embedding embedTextSegment(TextSegment segment);
    List<Embedding> embedTextSegments(List<TextSegment> segments);
}