package org.shark.evolvai.inference.adapter.in.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.shark.evolvai.inference.controller.QueryResponse;
import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.inference.port.input.InferenceUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class InferenceControllerTest {

    @Mock
    private InferenceUseCase useCase;

    @InjectMocks
    private InferenceController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void queryDelegatesToUseCase() {
        RagQueryRequest request = new RagQueryRequest();
        request.setQuery("hola");
        QueryResponse expected = new QueryResponse("respuesta", null, "conv1");
        when(useCase.query(request)).thenReturn(expected);

        ResponseEntity<QueryResponse> response = controller.query(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void advancedQueryDelegatesToUseCase() {
        RagQueryRequest request = new RagQueryRequest();
        request.setQuery("hola avanzada");
        QueryResponse expected = new QueryResponse("ok", null, "conv2");
        when(useCase.advancedQuery(request)).thenReturn(expected);

        ResponseEntity<QueryResponse> response = controller.advancedQuery(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }
}
