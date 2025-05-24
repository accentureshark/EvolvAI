package org.shark.evolvai.embedding.port.output;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import java.util.List;


public interface EmbeddingStorage {
    void store(String id, Embedding embedding, String text);

    List<EmbeddingMatch<String>> findSimilar(Embedding embedding, int maxResults, double minScore);

    List<String> findAllDocumentIds();

    List<EmbeddingMatch<String>> findSimilar(Embedding embedding, int maxResults, double minScore, Metadata filter);

    void removeAll();

    void store(String id, Embedding embedding, String text, Metadata metadata);

}
