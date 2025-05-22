package org.shark.evolvai.embedding.port.output;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import java.util.List;

public interface EmbeddingStorage {
    void store(String id, Embedding embedding, String text);

    List<EmbeddingMatch<String>> findSimilar(Embedding embedding, int maxResults, double minScore);
    List<String>findAllDocumentIds();
    void removeAll();
}
