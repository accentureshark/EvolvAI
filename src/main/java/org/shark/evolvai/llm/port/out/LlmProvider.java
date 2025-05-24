package org.shark.evolvai.llm.port.out;

import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

public interface LlmProvider {

    ChatMessage send(List<ChatMessage> messages);

    public String generateResponse(List<ChatMessage> context, String query, String customPrompt) ;

    String getDefaultPrompt();
}
