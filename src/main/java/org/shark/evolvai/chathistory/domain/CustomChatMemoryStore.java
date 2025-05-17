package org.shark.evolvai.chathistory.domain;

import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import java.util.List;

public interface CustomChatMemoryStore extends ChatMemoryStore {
    // Método personalizado que ya has implementado en provider/InMemoryChatMemoryStore
    List<String> getRelevantMemory(String userQuery);
}