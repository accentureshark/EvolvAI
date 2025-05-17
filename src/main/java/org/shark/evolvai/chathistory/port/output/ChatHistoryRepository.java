package org.shark.evolvai.chathistory.port.output;

public interface ChatHistoryRepository {
    void saveInteraction(String query, String answer);
}
