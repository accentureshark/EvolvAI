package org.shark.evolvai.llm.port.out;

import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

public interface LlmProvider {
    ChatMessage send(List<ChatMessage> messages);

    // Sin prompt personalizado (usa el prompt por defecto)
    String generateResponse(String context, String query);

    // Con prompt personalizado
    String generateResponse(String context, String query, String customPrompt);

    // Sin prompt personalizado, con historial
    String generateResponseWithHistory(String context, String query, String conversationHistory);

    // Con prompt personalizado y con historial
    String generateResponseWithHistory(String context, String query, String conversationHistory, String customPrompt);
}
