package org.shark.evolvai.inference.model;

import dev.langchain4j.data.message.ChatMessage;
import java.util.List;
import java.util.Objects;

/**
 * Contexto que se pasa al LLM: query enriquecida, contexto, matches,
 * conversationId e historial.
 */
public class EmbeddingContext {

    private final String enrichedQuery;
    private final String enrichedQueryWithContext;
    private final String context;
    private final List<?> matchDtos;
    private final String conversationId;
    private final List<ChatMessage> conversationHistory;

    public EmbeddingContext(String enrichedQuery,
                            String enrichedQueryWithContext,
                            String context,
                            List<?> matchDtos,
                            String conversationId,
                            List<ChatMessage> conversationHistory) {
        this.enrichedQuery = enrichedQuery;
        this.enrichedQueryWithContext = enrichedQueryWithContext;
        this.context = context;
        this.matchDtos = matchDtos;
        this.conversationId = conversationId;
        this.conversationHistory = conversationHistory;
    }

    public String enrichedQuery() { return enrichedQuery; }
    public String enrichedQueryWithContext() { return enrichedQueryWithContext; }
    public String context() { return context; }
    public List<?> matchDtos() { return matchDtos; }
    public String conversationId() { return conversationId; }
    public List<ChatMessage> conversationHistory() { return conversationHistory; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmbeddingContext)) return false;
        EmbeddingContext that = (EmbeddingContext) o;
        return Objects.equals(enrichedQuery, that.enrichedQuery) &&
                Objects.equals(conversationId, that.conversationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enrichedQuery, conversationId);
    }

    @Override
    public String toString() {
        return "EmbeddingContext{" +
                "query='" + enrichedQuery + '\'' +
                ", conversationId='" + conversationId + '\'' +
                '}';
    }
}
