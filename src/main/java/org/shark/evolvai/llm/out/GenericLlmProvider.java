package org.shark.evolvai.llm.out;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.shark.evolvai.llm.exception.ModelNotLoadedException;
import org.shark.evolvai.llm.port.out.LlmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Component
public class GenericLlmProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(GenericLlmProvider.class);

    private final ChatLanguageModel chatLanguageModel;
    private final String modelName;
    private final String ollamaBaseUrl;

    public GenericLlmProvider(
            ChatLanguageModel chatLanguageModel,
            @Value("${llm.ollama.model:phi3:mini}") String modelName,
            @Value("${llm.ollama.base-url:http://localhost:11434}") String ollamaBaseUrl
    ) {
        this.chatLanguageModel = chatLanguageModel;
        this.modelName = modelName;
        this.ollamaBaseUrl = ollamaBaseUrl;
    }

    @Override
    public ChatMessage send(List<ChatMessage> messages) {
        log.info("Enviando mensajes al modelo genérico: {}", messages);
        if (!isModelLoaded(modelName)) {
            throw new ModelNotLoadedException(modelName);
        }
        try {
            Response<AiMessage> response = chatLanguageModel.generate(messages);
            return response.content();
        } catch (RuntimeException e) {
            log.error("Error al consultar el modelo LLM", e);
            throw e;
        }
    }

    @Override
    public String generateResponse(String context, String query) {
        if (!isModelLoaded(modelName)) {
            throw new ModelNotLoadedException(modelName);
        }
        try {
            List<ChatMessage> messages = List.of(
                    new UserMessage(context + "\n" + query)
            );
            Response<AiMessage> response = chatLanguageModel.generate(messages);
            return response.content().text();
        } catch (RuntimeException e) {
            log.error("Error al generar respuesta con LLM", e);
            throw e;
        }
    }

    @Override
    public String generateResponseWithHistory(String context, String query, String history) {
        if (!isModelLoaded(modelName)) {
            throw new ModelNotLoadedException(modelName);
        }
        try {
            String prompt = (history != null && !history.isEmpty())
                    ? history + "\n" + context + "\n" + query
                    : context + "\n" + query;
            List<ChatMessage> messages = List.of(new UserMessage(prompt));
            Response<AiMessage> response = chatLanguageModel.generate(messages);
            return response.content().text();
        } catch (RuntimeException e) {
            log.error("Error al generar respuesta con historial en LLM", e);
            throw e;
        }
    }

    private boolean isModelLoaded(String modelName) {
        try {
            URL url = new URL(ollamaBaseUrl + "/api/tags");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String response = in.lines().reduce("", (a, b) -> a + b);
                return response.contains("\"" + modelName + "\"");
            }
        } catch (Exception e) {
            log.error("No se pudo verificar si el modelo está cargado en Ollama", e);
            return false;
        }
    }
}