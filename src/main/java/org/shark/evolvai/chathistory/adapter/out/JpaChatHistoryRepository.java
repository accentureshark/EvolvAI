package org.shark.evolvai.chathistory.adapter.out;

import org.shark.evolvai.chathistory.port.output.ChatHistoryRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "chatmemory-persistence.provider", havingValue = "jpa")
public class JpaChatHistoryRepository implements ChatHistoryRepository {

    private final ChatHistoryJpaRepository chatHistoryJpaRepository;

    public JpaChatHistoryRepository(ChatHistoryJpaRepository chatHistoryJpaRepository) {
        this.chatHistoryJpaRepository = chatHistoryJpaRepository;
    }

    @Override
    public void saveInteraction(String query, String answer) {
        String conversationId = UUID.randomUUID().toString();
        saveInteractionWithId(conversationId, query, answer);
    }

    @Override
    public String getConversationHistory(String conversationId) {
        List<ChatHistoryEntity> history = chatHistoryJpaRepository.findByConversationIdOrderByTimestampAsc(conversationId);
        if (history == null || history.isEmpty()) {
            return "";
        }

        StringBuilder historyBuilder = new StringBuilder();
        for (ChatHistoryEntity entry : history) {
            historyBuilder.append("Usuario: ").append(entry.getUserQuery()).append("\n");
            historyBuilder.append("AI: ").append(entry.getAiResponse()).append("\n");
        }
        return historyBuilder.toString();
    }

    @Override
    public void saveInteractionWithId(String conversationId, String query, String answer) {
        ChatHistoryEntity chatHistory = new ChatHistoryEntity();
        chatHistory.setConversationId(conversationId);
        chatHistory.setUserQuery(query);
        chatHistory.setAiResponse(answer);
        chatHistory.setTimestamp(LocalDateTime.now());
        chatHistoryJpaRepository.save(chatHistory);
    }
}