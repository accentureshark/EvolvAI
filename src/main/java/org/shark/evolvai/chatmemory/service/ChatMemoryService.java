package org.shark.evolvai.chatmemory.service;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMemoryService {
    private final ChatMemoryStore chatMemoryStore;

    public ChatMemoryService(ChatMemoryStore chatMemoryStore) {
        this.chatMemoryStore = chatMemoryStore;
    }

    public List<ChatMessage> getMessages(Object id) {
        return chatMemoryStore.getMessages(id);
    }

    public void updateMessages(Object id, List<ChatMessage> messages) {
        chatMemoryStore.updateMessages(id, messages);
    }

    public void deleteMessages(Object id) {
        chatMemoryStore.deleteMessages(id);
    }
}