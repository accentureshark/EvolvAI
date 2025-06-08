package org.shark.evolvai.chathistory.service;


import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.shark.evolvai.chathistory.port.in.ChatMemoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMemoryServiceImpl implements ChatMemoryService {

    private final ChatMemoryStore chatMemoryStore;

    public ChatMemoryServiceImpl(ChatMemoryStore chatMemoryStore) {
        this.chatMemoryStore = chatMemoryStore;
    }

    @Override
    public List<ChatMessage> getMessages(String conversationId) {
        return chatMemoryStore.getMessages(conversationId);
    }

    @Override
    public void updateMessages(String conversationId, List<ChatMessage> messages) {
        chatMemoryStore.updateMessages(conversationId, messages);
    }

    @Override
    public List<ChatMessage> getAllMessages() {
        if (chatMemoryStore instanceof org.shark.evolvai.chathistory.adapter.out.PostgresChatMemoryStore store) {
            return store.getAllMessages();
        }
        throw new UnsupportedOperationException("No soportado para este tipo de ChatMemoryStore");
    }
}


