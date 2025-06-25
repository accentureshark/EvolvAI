package org.shark.evolvai.embedding.port.in;

import java.util.List;
import java.util.Map;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;

public interface EmbeddingUseCase {

    void indexDocument(String id, String text, String customPrompt);

    void indexDocument(
        String id, String text, String customPrompt, Map<String, String> extraMetadata
    );

    void index(List<TextSegment> segments);

    List<String> listDocumentIds();

    List<String> findSimilarDocuments(String query, int maxResults, double minScore);

    Embedding generateEmbedding(String text);

    Embedding generateEmbedding(int[] inputIds, int[] attentionMask);

    void removeAllDocuments();
    
    void removeDocumentById(String documentId);

}
