package org.shark.evolvai.embedding.port.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingMatch;


public interface EmbeddingStorage {
    void store(String id, Embedding embedding, String text);

    void store(String id, Embedding embedding, String text, Metadata metadata);

    List<EmbeddingMatch<String>> findSimilar(Embedding embedding, int maxResults, double minScore);

    List<EmbeddingMatch<String>> findSimilar(
        Embedding embedding, int maxResults, double minScore, Metadata filter
    );

    List<String> findAllDocumentIds();

    void removeAll();

    Optional<Map<String, Object>> findMetadataByDocumentId(String documentId);
}
