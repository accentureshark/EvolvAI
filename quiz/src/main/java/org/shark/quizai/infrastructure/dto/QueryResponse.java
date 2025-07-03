package org.shark.quizai.infrastructure.dto;

import java.util.List;

/**
 * Minimal response object returned by the RAG service.
 */
public class QueryResponse {
    private String answer;
    private List<Object> matches;
    private String conversationId;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<Object> getMatches() {
        return matches;
    }

    public void setMatches(List<Object> matches) {
        this.matches = matches;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
