package org.shark.evolvai.chathistory.provider;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.shark.evolvai.chathistory.util.JsonUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryChatMemoryStore implements ChatMemoryStore {

    private final Map<String, String> memoryStore = new ConcurrentHashMap<>();

    @Override
    public List<ChatMessage> getMessages(Object id) {
        String json = memoryStore.get(id.toString());
        return JsonUtil.deserializeMessages(json);
    }

    @Override
    public void updateMessages(Object id, List<ChatMessage> messages) {
        String json = JsonUtil.serializeMessages(messages);
        memoryStore.put(id.toString(), json);
    }

    @Override
    public void deleteMessages(Object id) {
        memoryStore.remove(id.toString());
    }

    // Método opcional para testear relevancia si lo usás en tus pruebas
    public List<String> getRelevantMemory(String consulta) {
        return memoryStore.keySet().stream()
                .filter(key -> key.contains(consulta))
                .toList();
    }

    public void clearAll() {
        memoryStore.clear();
    }
}
