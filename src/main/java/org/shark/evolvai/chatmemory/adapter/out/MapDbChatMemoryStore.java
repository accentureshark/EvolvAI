package org.shark.evolvai.chatmemory.adapter.out;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.shark.evolvai.chatmemory.util.JsonUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

@Component
public class MapDbChatMemoryStore implements ChatMemoryStore {

    private final DB db;
    private final ConcurrentMap<String, String> store;

    public MapDbChatMemoryStore() {
        this.db = DBMaker.fileDB("chat-memory.db")
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
