package org.shark.evolvai.chathistory.config;

import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryStoreFactory {

    ///private final DynamoDbChatMemoryStore dynamoDbChatMemoryStore;
    // @Value("${chatmemory-persistence.provider:memory}")
    private String persistenceProvider;


    @Bean
    public ChatMemoryStore chatMemoryStore() {
        //if ("dynamodb".equalsIgnoreCase(persistenceProvider)) {
        //    return dynamoDbChatMemoryStore;
        //} else {
        return new InMemoryChatMemoryStore();
        //}
    }
}

