package org.shark.evolvai.llm.out;

import org.shark.evolvai.config.RagProperties;
import org.shark.evolvai.llm.port.out.StreamingLlmProvider;
import dev.langchain4j.data.message.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OllamaStreamingProvider implements StreamingLlmProvider {

    private static final Logger log = LoggerFactory.getLogger(OllamaStreamingProvider.class);
    private final WebClient webClient;
    private final RagProperties ragProperties;

    public OllamaStreamingProvider(RagProperties ragProperties) {
        this.ragProperties = ragProperties;
        String baseUrl = ragProperties.getLlm().getOllama().getBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        this.webClient = WebClient.create(baseUrl);
    }

    @Override
    public Flux<String> streamResponse(List<ChatMessage> context, String prompt) {
        Map<String, Object> body = new HashMap<>();
        var ollama = ragProperties.getLlm().getOllama();

        body.put("model", ollama.getModel());
        body.put("prompt", prompt);
        body.put("stream", true);
        if (ollama.getTemperature() > 0) {
            body.put("temperature", ollama.getTemperature());
        }

        return webClient.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnSubscribe(sub -> log.info("Enviando request streaming a Ollama"))
                .doOnNext(chunk -> log.debug("Chunk recibido: {}", chunk))
                .doOnError(e -> log.error("Error en streaming Ollama", e));
    }
}
