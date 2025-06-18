package org.shark.evolvai.inference.adapter.in.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.inference.port.input.InferenceUseCase;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

class InferenceStreamControllerTest {

    @Mock
    private InferenceUseCase useCase;

    private InferenceStreamController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new InferenceStreamController(useCase);
    }

    @Test
    void queryStreamDelegatesToUseCase() {
        RagQueryRequest request = new RagQueryRequest();
        request.setQuery("stream");
        Flux<String> flux = Flux.just("uno", "dos");
        when(useCase.queryStream(request)).thenReturn(flux);

        StepVerifier.create(controller.queryStream(request))
                .expectNext("uno")
                .expectNext("dos")
                .verifyComplete();
    }
}
