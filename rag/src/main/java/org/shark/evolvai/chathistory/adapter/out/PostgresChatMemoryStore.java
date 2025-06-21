package org.shark.evolvai.chathistory.adapter.out;

import java.util.Collections;
import java.util.List;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.shark.evolvai.chathistory.util.JsonUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "chatmemory-persistence.provider", havingValue = "jpa")
public class PostgresChatMemoryStore implements ChatMemoryStore {

    private final ChatMemoryJpaRepository repository;

    public PostgresChatMemoryStore(ChatMemoryJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(Object id) {
        return repository.findById(id.toString())
                .map(entity -> JsonUtil.deserializeMessages(entity.getMemoryJson()))
                .orElse(Collections.emptyList());
    }

    @Override
    @Transactional
    public void updateMessages(Object id, List<ChatMessage> messages) {
        try {
            String json = JsonUtil.serializeMessages(messages);
            repository.save(new ChatMemoryEntity(id.toString(), json));
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar mensajes", e);
        }
    }

    @Override
    @Transactional
    public void deleteMessages(Object id) {
        repository.deleteById(id.toString());
    }

    @Transactional
    public List<ChatMessage> getAllMessages() {
        // ver de agrupar por conversationId

        return repository.findAll().stream()
                .flatMap(entity -> JsonUtil.deserializeMessages(entity.getMemoryJson()).stream())
                .toList();
    }
}