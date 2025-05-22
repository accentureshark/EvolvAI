package org.shark.evolvai.embedding.port.input;


import dev.langchain4j.data.embedding.Embedding;

import java.util.List;

public interface EmbeddingUseCase {
    Embedding generateEmbedding(int[] inputIds, int[] attentionMask);

    void indexDocument(String id, String text, String customPrompt);

    List<String> findSimilarDocuments(String query, int maxResults, double minScore);

    Embedding generateEmbedding(String text);

    List<String> listDocumentIds();

    void removeAllDocuments();
}