package org.shark.evolvai.llm.out;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.shark.evolvai.llm.port.out.LlmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LlamaLlmProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(LlamaLlmProvider.class);

    private final OpenAiChatModel llamaModel;

    public LlamaLlmProvider(OpenAiChatModel llamaModel) {
        this.llamaModel = llamaModel;
    }

    @Override
    public ChatMessage send(List<ChatMessage> messages) {
        log.info("Enviando mensajes al modelo Llama: {}", messages);
        ChatMessage response = llamaModel.generate(messages).content();
        log.info("Respuesta recibida del modelo Llama: {}", response);
        return response;
    }

    @Override
    public String generateResponse(String context, String query) {
        log.info("Generando respuesta con contexto. Query: '{}', Contexto: '{}'", query, context);
        List<ChatMessage> messages = List.of(
                new SystemMessage("Usa esta información para responder a la pregunta del usuario: " + context),
                new UserMessage(query)
        );
        String result = llamaModel.generate(messages).content().text();
        log.info("Respuesta generada: {}", result);
        return result;
    }

    @Override
    public String generateResponseWithHistory(String context, String query, String conversationHistory) {
        log.info("Generando respuesta con contexto e historial. Query: '{}', Contexto: '{}', Historial: '{}'", query, context, conversationHistory);
        List<ChatMessage> messages = List.of(
                new SystemMessage("Usa esta información para responder a la pregunta del usuario: " + context +
                        "\n\nHistorial de conversación:\n" + conversationHistory),
                new UserMessage(query)
        );
        String result = llamaModel.generate(messages).content().text();
        log.info("Respuesta generada con historial: {}", result);
        return result;
    }
}