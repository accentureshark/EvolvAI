package org.shark.evolvai.embedding.adapter.output.storage;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.shark.evolvai.embedding.port.output.EmbeddingStorage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InMemoryEmbeddingStorage implements EmbeddingStorage {

    private final InMemoryEmbeddingStore<TextSegment> embeddingStore;

    public InMemoryEmbeddingStorage() {
        this.embeddingStore = new InMemoryEmbeddingStore<>();
    }

    @Override
    public void store(String id, Embedding embedding, String text) {
        embeddingStore.add(id, embedding, TextSegment.from(text));
    }

    @Override
    public void store(String id, Embedding embedding, String text, Metadata metadata) {
        embeddingStore.add(id, embedding, new TextSegment(text, metadata));
    }

    @Override
    public List<EmbeddingMatch<String>> findSimilar(Embedding embedding, int maxResults, double minScore) {
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(embedding, maxResults);

        return matches.stream()
                .filter(match -> match.score() >= minScore)
                .map(match -> new EmbeddingMatch<>(
                        match.score(),
                        match.embeddingId(),
                        match.embedding(),
                        match.embedded() != null ? match.embedded().text() : ""
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<EmbeddingMatch<String>> findSimilar(Embedding embedding, int maxResults, double minScore, Metadata filter) {
        return embeddingStore.findRelevant(embedding, maxResults).stream()
                .filter(match -> match.score() >= minScore)
                .filter(match -> {
                    for (String key : filter.asMap().keySet()) {
                        Object expected = filter.get(key);
                        Object actual = match.embedded().metadata().get(key);
                        if (actual == null || !actual.toString().equals(expected.toString())) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(match -> new EmbeddingMatch<>(
                        match.score(),
                        match.embeddingId(),
                        match.embedding(),
                        match.embedded() != null ? match.embedded().text() : ""
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findAllDocumentIds() {
        // entries() no está disponible en esta versión
        throw new UnsupportedOperationException("findAllDocumentIds() no es compatible con esta versión de InMemoryEmbeddingStore");
    }

    @Override
    public void removeAll() {
        // clear() no está disponible en esta versión
        throw new UnsupportedOperationException("removeAll() no es compatible con esta versión de InMemoryEmbeddingStore");
    }    }
