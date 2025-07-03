package org.shark.quizai.infrastructure.dto;

import java.util.Map;

/**
 * Minimal request object used to call the RAG service.
 */
public class RagQueryRequest {
    private String query;
    private String conversationId;
    private int maxResults;
    private double minSimilarity;
    private boolean includeMatches;
    private String customPrompt;
    private String documentId;
    private Map<String, String> contextMetadata;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public double getMinSimilarity() {
        return minSimilarity;
    }

    public void setMinSimilarity(double minSimilarity) {
        this.minSimilarity = minSimilarity;
    }

    public boolean isIncludeMatches() {
        return includeMatches;
    }

    public void setIncludeMatches(boolean includeMatches) {
        this.includeMatches = includeMatches;
    }

    public String getCustomPrompt() {
        return customPrompt;
    }

    public void setCustomPrompt(String customPrompt) {
        this.customPrompt = customPrompt;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Map<String, String> getContextMetadata() {
        return contextMetadata;
    }

    public void setContextMetadata(Map<String, String> contextMetadata) {
        this.contextMetadata = contextMetadata;
    }
}
