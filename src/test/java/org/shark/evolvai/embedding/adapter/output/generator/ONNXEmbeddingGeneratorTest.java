package org.shark.evolvai.embedding.adapter.output.generator;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shark.evolvai.embedding.adapter.output.generator.ONNXEmbeddingGenerator;
import org.shark.evolvai.embedding.provider.ONNXProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ONNXEmbeddingGeneratorTest {

    @Mock
    private ONNXProvider onnxProvider;

    private ONNXEmbeddingGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ONNXEmbeddingGenerator(onnxProvider);
    }


    @Test
    void testGenerateEmbedding() {
        // Arrange
        String text = "texto de prueba";
        float[] vector = new float[]{0.1f, 0.2f, 0.3f};
        Embedding embedding = Embedding.from(vector);
        Response<Embedding> response = Response.from(embedding); // Usa el método adecuado para crear el Response
        when(onnxProvider.embed(text)).thenReturn(response);

        // Act
        Embedding result = generator.generateEmbedding(text);

        // Assert
        assertNotNull(result);
        assertEquals(embedding, result);
        verify(onnxProvider).embed(text);
    }
    // Más tests...
}