package org.shark.evolvai.embedding.port.input;


import dev.langchain4j.data.embedding.Embedding;

import java.util.List;

public interface EmbeddingUseCase {
    Embedding generateEmbedding(int[] inputIds, int[] attentionMask);

    void indexDocument(String id, String text);

    List<String> findSimilarDocuments(String query, int maxResults, double minScore);

    Embedding generateEmbedding(String text);

    void removeAllDocuments();
}