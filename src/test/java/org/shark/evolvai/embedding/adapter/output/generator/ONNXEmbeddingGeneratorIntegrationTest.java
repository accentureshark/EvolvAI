package org.shark.evolvai.embedding.adapter.output.generator;

import dev.langchain4j.data.embedding.Embedding;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.shark.evolvai.embedding.provider.ONNXProvider;

import java.net.URL;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class ONNXEmbeddingGeneratorIntegrationTest {

    private static ONNXProvider onnxProvider;

    @BeforeAll
    static void setUp() throws Exception {
        // Cargar el modelo ONNX desde el classpath
        ClassLoader classLoader = ONNXEmbeddingGeneratorIntegrationTest.class.getClassLoader();
        URL resource = classLoader.getResource("all-MiniLM-L6-v2.onnx");
        assertNotNull(resource, "No se encontró el modelo ONNX en resources");
        String modelPath = Paths.get(resource.toURI()).toString();

        // Instanciar el provider y setear el path manualmente
        onnxProvider = new ONNXProvider();
        java.lang.reflect.Field field = ONNXProvider.class.getDeclaredField("modelPath");
        field.setAccessible(true);
        field.set(onnxProvider, modelPath);

        // Inicializar el entorno ONNX
        onnxProvider.initialize();
    }

    @AfterAll
    static void tearDown() {
        onnxProvider.cleanup();
    }

    @Test
    void testEmbeddingWithRealModel() {
        String texto = "texto de prueba";
        Embedding embedding = onnxProvider.embedText(texto);
        assertNotNull(embedding);
        assertTrue(embedding.vector().length > 0);
    }
}