package org.shark.evolvai.inference.adapter.in.rest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shark.evolvai.inference.controller.QueryResponse;
import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.inference.port.input.InferenceUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InferenceControllerTest {

    @Mock
    private InferenceUseCase inferenceUseCase;

    @InjectMocks
    private InferenceController inferenceController;

    @Test
    void queryReturnsOkWhenRequestIsValid() {
        RagQueryRequest request = new RagQueryRequest();
        QueryResponse expectedResponse = new QueryResponse("respuesta", Collections.emptyList(), "conv-1");
        when(inferenceUseCase.query(request)).thenReturn(expectedResponse);

        ResponseEntity<QueryResponse> response = inferenceController.query(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void advancedQueryReturnsOkWhenRequestIsValid() {
        RagQueryRequest request = new RagQueryRequest();
        QueryResponse expectedResponse = new QueryResponse("respuesta", Collections.emptyList(), "conv-2");
        when(inferenceUseCase.advancedQuery(request)).thenReturn(expectedResponse);

        ResponseEntity<QueryResponse> response = inferenceController.advancedQuery(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }
}