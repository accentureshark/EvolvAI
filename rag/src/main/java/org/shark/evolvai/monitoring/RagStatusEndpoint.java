package org.shark.evolvai.monitoring;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import org.shark.evolvai.embedding.port.output.EmbeddingStorage;
import org.shark.evolvai.llm.port.out.LlmProvider;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
@Endpoint(id = "rag-status")
@RequiredArgsConstructor
public class RagStatusEndpoint {

    private static final AtomicLong totalRequests = new AtomicLong();
    private static final AtomicLong totalProcessingTimeMillis = new AtomicLong();
    private final EmbeddingStorage embeddingStorage;
    private final LlmProvider llmProvider;

    // Para usar desde filtros/interceptors
    public static void registerRequest(Duration duration) {
        totalRequests.incrementAndGet();
        totalProcessingTimeMillis.addAndGet(duration.toMillis());
    }

    @ReadOperation
    public Map<String, Object> status() {
        Map<String, Object> info = new LinkedHashMap<>();

        List<String> docIds = embeddingStorage.findAllDocumentIds();

        info.put("embedding.document.total", docIds.size());
        info.put("embedding.document.ids", docIds.stream().limit(10).collect(Collectors.toList()));

        Map<String, Long> countsByPrefix = docIds.stream()
                .map(id -> id.split("///")[0])
                .collect(Collectors.groupingBy(x -> x, Collectors.counting()));
        info.put("embedding.fragments.perDocument", countsByPrefix);

        // Test de respuesta del modelo LLM
        try {
            Instant start = Instant.now();
            ChatMessage result = llmProvider.send(List.of(UserMessage.from("¿Estás vivo?")));
            long duration = Duration.between(start, Instant.now()).toMillis();
            info.put("llm.status", "OK");
            info.put("llm.response.ms", duration);
            info.put("llm.sample.output", result.text());
        } catch (Exception ex) {
            info.put("llm.status", "ERROR");
            info.put("llm.error", ex.getMessage());
        }

        long reqCount = totalRequests.get();
        long totalMillis = totalProcessingTimeMillis.get();
        info.put("requests.total", reqCount);
        info.put("processing.total.ms", totalMillis);
        info.put("processing.avg.ms", reqCount > 0 ? totalMillis / reqCount : 0);

        return info;
    }
}
