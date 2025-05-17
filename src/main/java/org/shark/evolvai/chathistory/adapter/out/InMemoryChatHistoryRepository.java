package org.shark.evolvai.chathistory.adapter.out;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.shark.evolvai.chathistory.port.output.ChatHistoryRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "chatmemory-persistence.provider", havingValue = "memory", matchIfMissing = true)
public class InMemoryChatHistoryRepository implements ChatHistoryRepository {

    private final MapDbChatMemoryStore chatMemoryStore;

    public InMemoryChatHistoryRepository(MapDbChatMemoryStore chatMemoryStore) {
        this.chatMemoryStore = chatMemoryStore;
    }

    @Override
    public void saveInteraction(String query, String answer) {
        String sessionId = UUID.randomUUID().toString();
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new UserMessage(query));
        messages.add(new AiMessage(answer));
        chatMemoryStore.updateMessages(sessionId, messages);
    }
}
