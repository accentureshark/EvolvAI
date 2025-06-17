package org.shark.evolvai.inference.provider;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import org.shark.evolvai.inference.port.ExternalDataProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class HttpMcpExternalDataProvider implements ExternalDataProvider {

    private final WebClient webClient = WebClient.create();

    @Override
    public List<TextSegment> fetch(String query, String mcpServerUrl, Map<String, Object> params) {
        String path = params.getOrDefault("endpoint", "/mcp/chunks").toString();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(mcpServerUrl)
                .path(path);

        params.forEach((key, value) -> {
            if (!"endpoint".equals(key)) {
                uriBuilder.queryParam(key, value);
            }
        });

        String url = uriBuilder.toUriString();

        Mono<ChunkResponse[]> monoResponse = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(ChunkResponse[].class);

        ChunkResponse[] response;
        try {
            response = monoResponse.block();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }

        List<TextSegment> result = new ArrayList<>();
        if (response != null) {
            for (ChunkResponse chunk : response) {
                result.add(TextSegment.from(chunk.content, Metadata.from(chunk.metadata)));
            }
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchMetadata(String mcpServerUrl) {
        String url = UriComponentsBuilder.fromHttpUrl(mcpServerUrl)
                .path("/mcp/metadata")
                .toUriString();

        Mono<Map> monoResponse = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class);

        Map<String, Object> response;
        try {
            response = monoResponse.block();
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of();
        }
        return response != null ? response : Map.of();
    }

    // DTO auxiliar solo para chunks, ya que es un estándar en LangChain4j
    public static class ChunkResponse {
        public String id;
        public String content;
        public Map<String, String> metadata;
    }
}
