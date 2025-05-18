package org.shark.evolvai.llm.out;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.shark.evolvai.llm.port.out.LlmProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LlamaLlmProvider implements LlmProvider {

    private final OpenAiChatModel llamaModel;

    public LlamaLlmProvider(OpenAiChatModel llamaModel) {
        this.llamaModel = llamaModel;
    }

    @Override
    public ChatMessage send(List<ChatMessage> messages) {
        return llamaModel.generate(messages).content();
    }

    @Override
    public String generateResponse(String context, String query) {
        List<ChatMessage> messages = List.of(
                new SystemMessage("Usa esta información para responder a la pregunta del usuario: " + context),
                new UserMessage(query)
        );
        return llamaModel.generate(messages).content().text();
    }

    @Override
    public String generateResponseWithHistory(String context, String query, String conversationHistory) {
        List<ChatMessage> messages = List.of(
                new SystemMessage("Usa esta información para responder a la pregunta del usuario: " + context +
                        "\n\nHistorial de conversación:\n" + conversationHistory),
                new UserMessage(query)
        );
        return llamaModel.generate(messages).content().text();
    }
}