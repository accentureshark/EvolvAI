package org.shark.evolvai.llm.out;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
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

    private final ChatModel chatModel;
    private final String modelName;
    private final String ollamaBaseUrl;
    private final String defaultPrompt;

    public GenericLlmProvider(
            ChatModel chatModel,
            @Value("${rag.llm.ollama.model}") String modelName,
            @Value("${rag.llm.ollama.base-url}") String ollamaBaseUrl,
            @Value("${rag.llm.prompt}") String defaultPrompt
    ) {
        this.chatModel = chatModel;
        this.modelName = modelName;
        this.ollamaBaseUrl = ollamaBaseUrl;
        this.defaultPrompt = defaultPrompt;
    }

    @Override
    public ChatMessage send(List<ChatMessage> messages) {
        log.warn("⚠️ Método send(.{}.) fue invocado directamente. Esto omite el uso de prompt del YAML.",messages.toString());
        if (!isModelLoaded(modelName)) {
            throw new ModelNotLoadedException(modelName);
        }
        try {
            ChatResponse response = chatModel.chat(messages);
            log.info("Respuesta recibida del modelo: {}", response.aiMessage().text());
            return response.aiMessage();
        } catch (RuntimeException e) {
            log.error("Error al consultar el modelo LLM", e);
            throw e;
        }
    }


    /**
     * Nuevo método: genera una respuesta usando una lista de ChatMessage como contexto.
     */
    public String generateResponse(List<ChatMessage> context, String query) {
        if (!isModelLoaded(modelName)) {
            throw new ModelNotLoadedException(modelName);
        }
        try {
            // Construir la lista de mensajes: prompt como primer mensaje, luego contexto, luego query
            List<ChatMessage> messages = new java.util.ArrayList<>();

            if (context != null && !context.isEmpty()) {
                messages.addAll(context);
            }
            messages.add(new UserMessage(query));
            log.info("Enviando mensaje al modelo: {}", messages);
            ChatResponse response = chatModel.chat(messages);
            log.info("Respuesta generada: {}", response.aiMessage().text());
            return response.aiMessage().text();
        } catch (RuntimeException e) {
            log.error("Error al generar respuesta con LLM", e);
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
