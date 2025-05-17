package org.shark.evolvai.chathistory.adapter.out;

import org.springframework.beans.factory.annotation.Value;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.shark.evolvai.chathistory.util.JsonUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
@Component
@ConditionalOnProperty(name = "chatmemory-persistence.provider", havingValue = "memory", matchIfMissing = true)
public class MapDbChatMemoryStore implements ChatMemoryStore {

    private final DB db;
    private final ConcurrentMap<String, String> store;

    public MapDbChatMemoryStore(@Value("${chatmemory-persistence.memory.file-path:chat-memory.db}") String filePath) {
        this.db = DBMaker.fileDB(filePath)
                .closeOnJvmShutdown()
                .make();
        this.store = db.hashMap("chat-memory", Serializer.STRING, Serializer.STRING)
                .createOrOpen();
    }

    @Override
    public List<ChatMessage> getMessages(Object id) {
        String json = store.get(id.toString());
        return JsonUtil.deserializeMessages(json);
    }

    @Override
    public void updateMessages(Object id, List<ChatMessage> messages) {
        String json = JsonUtil.serializeMessages(messages);
        store.put(id.toString(), json);
        db.commit();
    }

    @Override
    public void deleteMessages(Object id) {
        store.remove(id.toString());
        db.commit();
    }

    public void close() {
        if (db != null && !db.isClosed()) {
            db.close();
        }
    }
}