package org.shark.quizai.infrastructure;

import org.shark.quizai.infrastructure.dto.RagQueryRequest;
import org.shark.quizai.infrastructure.dto.QueryResponse;
import org.shark.quizai.domain.model.QuizResponseRequest;
import org.shark.quizai.domain.port.out.InferenceClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class InferenceClientImpl implements InferenceClient {

    private final WebClient webClient;

    public InferenceClientImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build(); // Cambia el puerto si es necesario
    }

    @Override
    public String inferResult(QuizResponseRequest request) {
        RagQueryRequest ragRequest = new RagQueryRequest();
        ragRequest.setQuery(request.getQuery());
        ragRequest.setDocumentId(request.getDocumentId());
        ragRequest.setConversationId(request.getConversationId());
        ragRequest.setCustomPrompt(request.getCustomPrompt());
        ragRequest.setIncludeMatches(false);

        Mono<QueryResponse> responseMono = webClient.post()
                .uri("/api/inference/query")
                .bodyValue(ragRequest)
                .retrieve()
                .bodyToMono(QueryResponse.class);

        QueryResponse response = responseMono.block();
        return response != null ? response.getAnswer() : null;
    }
}
