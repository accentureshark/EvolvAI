package org.shark.evolvai.llm.port.out;

import java.util.List;

import dev.langchain4j.data.message.ChatMessage;

public interface LlmProvider {

    ChatMessage send(List<ChatMessage> messages);

    String generateResponse(List<ChatMessage> context, String query);

    String getDefaultPrompt();
}
