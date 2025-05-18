package org.shark.evolvai.chathistory.port.output;

public interface ChatHistoryRepository {
    void saveInteraction(String query, String answer);

    // Añadir estos métodos
    String getConversationHistory(String conversationId);
    void saveInteractionWithId(String conversationId, String query, String answer);
}