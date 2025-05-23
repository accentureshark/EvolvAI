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
    private final String defaultPrompt;

    public GenericLlmProvider(
            ChatLanguageModel chatLanguageModel,
            @Value("${llm.ollama.model:phi3:mini}") String modelName,
            @Value("${llm.ollama.base-url:http://localhost:11434}") String ollamaBaseUrl,
            @Value("${llm.default-prompt:Responde de manera clara y profesional:}") String defaultPrompt
    ) {
        this.chatLanguageModel = chatLanguageModel;
        this.modelName = modelName;
        this.ollamaBaseUrl = ollamaBaseUrl;
        this.defaultPrompt = defaultPrompt;
    }

    @Override
    public ChatMessage send(List<ChatMessage> messages) {
        log.warn("⚠️ Método send(...) fue invocado directamente. Esto omite el uso de prompt del YAML.");
        if (!isModelLoaded(modelName)) {
            throw new ModelNotLoadedException(modelName);
        }
        try {
            Response<AiMessage> response = chatLanguageModel.generate(messages);
            log.info("Respuesta recibida del modelo: {}", response.content().text());
            return response.content();
        } catch (RuntimeException e) {
            log.error("Error al consultar el modelo LLM", e);
            throw e;
        }
    }

    @Override
    public String generateResponse(String context, String query) {
        return generateResponse(context, query, null);
    }

    @Override
    public String generateResponse(String context, String query, String customPrompt) {
        if (!isModelLoaded(modelName)) {
            throw new ModelNotLoadedException(modelName);
        }
        try {
            String prompt = (customPrompt != null && !customPrompt.isBlank()) ? customPrompt : defaultPrompt;
            log.info("Prompt usado para LLM: {}", prompt);
            List<ChatMessage> messages = List.of(new UserMessage(prompt + "\n" + context + "\n" + query));
            log.info("Enviando mensaje al modelo: {}", messages);
            Response<AiMessage> response = chatLanguageModel.generate(messages);
            log.info("Respuesta generada: {}", response.content().text());
            return response.content().text();
        } catch (RuntimeException e) {
            log.error("Error al generar respuesta con LLM", e);
            throw e;
        }
    }

    @Override
    public String generateResponseWithHistory(String context, String query, String history) {
        return generateResponseWithHistory(context, query, history, null);
    }

    @Override
    public String generateResponseWithHistory(String context, String query, String history, String customPrompt) {
        if (!isModelLoaded(modelName)) {
            throw new ModelNotLoadedException(modelName);
        }
        try {
            String prompt = (customPrompt != null && !customPrompt.isBlank()) ? customPrompt : defaultPrompt;
            String fullPrompt = (history != null && !history.isEmpty())
                    ? prompt + "\n" + history + "\n" + context + "\n" + query
                    : prompt + "\n" + context + "\n" + query;
            log.info("Prompt usado para LLM (con historial): {}", prompt);
            log.debug("Prompt completo enviado al modelo: {}", fullPrompt);
            List<ChatMessage> messages = List.of(new UserMessage(fullPrompt));
            log.info("Enviando mensaje al modelo: {}", messages);
            Response<AiMessage> response = chatLanguageModel.generate(messages);
            log.info("Respuesta generada: {}", response.content().text());
            return response.content().text();
        } catch (RuntimeException e) {
            log.error("Error al generar respuesta con historial en LLM", e);
            throw e;
        }
    }

    @Override
    public String getDefaultPrompt() {
        return defaultPrompt;
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
                boolean loaded = response.contains("\"" + modelName + "\"");
                log.debug("¿Modelo '{}' cargado en Ollama?: {}", modelName, loaded);
                return loaded;
            }
        } catch (Exception e) {
            log.error("No se pudo verificar si el modelo está cargado en Ollama", e);
            return false;
        }
    }
}