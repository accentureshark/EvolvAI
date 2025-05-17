package org.shark.evolvai.embedding.port.output;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;

import java.util.List;

public interface EmbeddingGenerator {
    Embedding generateEmbedding(String text);
    Embedding generateEmbedding(TextSegment segment);
    // Mantener solo uno de estos dos métodos (son idénticos pero con nombres de parámetros diferentes)
    List<Embedding> generateEmbeddings(List<TextSegment> segments);
}