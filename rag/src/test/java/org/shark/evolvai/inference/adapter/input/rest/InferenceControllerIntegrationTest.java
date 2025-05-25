package org.shark.evolvai.inference.adapter.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.shark.evolvai.inference.controller.QueryResponse;
import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InferenceControllerIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(InferenceControllerIntegrationTest.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void queryEndpointReturnsValidResponse() {
        RagQueryRequest request = new RagQueryRequest();
        request.setQuery("¿Cuál es la capital de Francia?"); // Campo obligatorio

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RagQueryRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<QueryResponse> response = restTemplate.postForEntity(
                "/api/inference/query", entity, QueryResponse.class);
        log.info("Respuesta del endpoint: {}", response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getAnswer());
        // Serializa la respuesta a JSON y verifica si contiene "Paris"
// Manejo de excepción
        try {
            String json = objectMapper.writeValueAsString(response.getBody());
            assertTrue(json.toLowerCase().contains("paris"), "La respuesta no contiene 'Paris'");

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            fail("Error serializando la respuesta a JSON: " + e.getMessage());
        }

    }

    @Test
    void ragEndpointReturnsValidResponse() {
        RagQueryRequest request = new RagQueryRequest();
        request.setQuery("Explica la fotosíntesis"); // Campo obligatorio

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RagQueryRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<QueryResponse> response = restTemplate.postForEntity(
                "/api/inference/rag", entity, QueryResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getAnswer());
    }

    @Test
    void queryEndpointReturnsBadRequestOnInvalidInput() {
        RagQueryRequest request = new RagQueryRequest(); // Sin campos requeridos

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RagQueryRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/inference/query", entity, String.class);

        assertTrue(response.getStatusCode().is4xxClientError());
    }
}