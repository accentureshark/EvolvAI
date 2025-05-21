package org.shark.evolvai.llm.out;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.shark.evolvai.llm.port.out.LlmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GenericLlmProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(GenericLlmProvider.class);

    private final ChatLanguageModel chatLanguageModel;

    public GenericLlmProvider(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    @Override
    public ChatMessage send(List<ChatMessage> messages) {
        log.info("Enviando mensajes al modelo genérico: {}", messages);
        Response<AiMessage> response = chatLanguageModel.generate(messages);
        //log.info("Respuesta recibida del modelo genérico: {}", response.content().toString());
        return response.content();
    }

    @Override
    public String generateResponse(String context, String query) {
        String prompt = String.format("""
            Context:
            %s

            Question:
            %s

            Answer based on the provided context:
            """, context, query);

        log.info("Generando respuesta con contexto. Query: '{}', Contexto: '{}'", query, context);
        Response<AiMessage> response = chatLanguageModel.generate(UserMessage.userMessage(prompt));
        log.info("Respuesta generada: {}", response.content().text());
        return response.content().text();
    }

    @Override
    public String generateResponseWithHistory(String context, String query, String conversationHistory) {
        String prompt = String.format("""
            Context:
            %s

            Conversation History:
            %s

            Question:
            %s

            Answer based on the provided context and conversation history:
            """, context, conversationHistory, query);

        log.info("Generando respuesta con contexto e historial. Query: '{}', Contexto: '{}', Historial: '{}'", query, context, conversationHistory);
        Response<AiMessage> response = chatLanguageModel.generate(UserMessage.userMessage(prompt));
        log.info("Respuesta generada con historial: {}", response.content().text());
        return response.content().text();
    }
}