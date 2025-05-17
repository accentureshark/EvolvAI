package org.shark.evolvai.chathistory.config;

import org.shark.evolvai.chathistory.adapter.out.MapDbChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;

@Configuration
public class ChatMemoryConfig {

    @PreDestroy
    public void closeResources(@Autowired(required = false) MapDbChatMemoryStore mapDbStore) {
        if (mapDbStore != null) {
            mapDbStore.close();
        }
    }
}