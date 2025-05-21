package org.shark.evolvai.embedding.provider;

import ai.onnxruntime.*;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ONNXProvider implements EmbeddingModel {


    private static final Logger logger = LoggerFactory.getLogger(ONNXProvider.class);


    private static final int MAX_TOKENS = 512;


    @Value("${embedding.onnx.model-path}")
    private String modelPath;

    private OrtEnvironment environment;
    private OrtSession session;

    @PostConstruct
    public void initialize() {
        try {
            logger.info("Initializing ONNX environment and session with model: {}", modelPath);
            Path path = Paths.get(modelPath);
            environment = OrtEnvironment.getEnvironment();
            session = environment.createSession(path.toString(), new OrtSession.SessionOptions());

            // Imprime la forma esperada de las entradas del modelo
            logger.info("Modelo ONNX - Información de entradas:");
            session.getInputInfo().forEach((name, info) -> {
                if (info.getInfo() instanceof TensorInfo tensorInfo) {
                    logger.info("Nombre: {}", name);
                    logger.info("Tipo: {}", tensorInfo.type);
                    logger.info("Forma esperada: {}", java.util.Arrays.toString(tensorInfo.getShape()));
                }
            });

            logger.info("ONNX session initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize ONNX session", e);
            throw new RuntimeException("Failed to initialize ONNX session", e);
        }
    }

    @Override
    public Response<Embedding> embed(String text) {
        try {
            String preprocessedText = preprocessText(text);

            // Tokenización simulada
            long[] inputIds = tokenizeText(preprocessedText);

            // Crear attention_mask y token_type_ids
            long[] attentionMask = new long[inputIds.length];
            long[] tokenTypeIds = new long[inputIds.length];
            java.util.Arrays.fill(attentionMask, 1);
            java.util.Arrays.fill(tokenTypeIds, 0);

            // Crear tensores 2D
            long[][] inputIdsBatch = new long[1][inputIds.length];
            long[][] attentionMaskBatch = new long[1][attentionMask.length];
            long[][] tokenTypeIdsBatch = new long[1][tokenTypeIds.length];
            inputIdsBatch[0] = inputIds;
            attentionMaskBatch[0] = attentionMask;
            tokenTypeIdsBatch[0] = tokenTypeIds;

            logger.info("Dimensiones reales de input_ids: [{}][{}]", inputIdsBatch.length, inputIdsBatch[0].length);
            logger.info("Dimensiones reales de attention_mask: [{}][{}]", attentionMaskBatch.length, attentionMaskBatch[0].length);
            logger.info("Dimensiones reales de token_type_ids: [{}][{}]", tokenTypeIdsBatch.length, tokenTypeIdsBatch[0].length);

            // Crear tensores ONNX
            OnnxTensor inputIdsTensor = OnnxTensor.createTensor(environment, inputIdsBatch);
            OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(environment, attentionMaskBatch);
            OnnxTensor tokenTypeIdsTensor = OnnxTensor.createTensor(environment, tokenTypeIdsBatch);

            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input_ids", inputIdsTensor);
            inputs.put("attention_mask", attentionMaskTensor);
            inputs.put("token_type_ids", tokenTypeIdsTensor);

            OrtSession.Result result = session.run(inputs);

            float[] embeddings = extractEmbeddings(result);

            inputIdsTensor.close();
            attentionMaskTensor.close();
            tokenTypeIdsTensor.close();
            result.close();

            return Response.from(new Embedding(embeddings));

        } catch (Exception e) {
            logger.error("Error embedding text", e);
            throw new RuntimeException("Failed to generate embedding", e);
        }
    }

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> segments) {
        List<Embedding> embeddings = new ArrayList<>();
        for (TextSegment segment : segments) {
            embeddings.add(embed(segment.text()).content());
        }
        return Response.from(embeddings);
    }

    public Embedding embedText(String text) {
        return embed(text).content();
    }

    public List<Embedding> embedTexts(List<String> texts) {
        List<Embedding> embeddings = new ArrayList<>();
        for (String text : texts) {
            embeddings.add(embed(text).content());
        }
        return embeddings;
    }

    private String preprocessText(String text) {
        return text.trim().toLowerCase();
    }

    private long[] tokenizeText(String text) {
        // Conversión básica de texto a tokens usando chars (tokenización simulada)
        char[] chars = text.toCharArray();

        // Truncamiento a MAX_TOKENS
        int maxLength = Math.min(chars.length, MAX_TOKENS);

        if (chars.length > MAX_TOKENS) {
            logger.warn("Texto truncado de {} a {} tokens", chars.length, MAX_TOKENS);
        }

        long[] tokens = new long[maxLength];
        for (int i = 0; i < maxLength; i++) {
            tokens[i] = chars[i];
        }
        return tokens;
    }


    private float[] extractEmbeddings(OrtSession.Result result) throws OrtException {
        Object value = result.get(0).getValue();
        if (value instanceof float[][] data) {
            return data[0];
        } else if (value instanceof float[][][] data3d) {
            return data3d[0][0];
        } else {
            throw new IllegalStateException("Tipo de salida inesperado: " + value.getClass());
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (session != null) {
                session.close();
            }
            if (environment != null) {
                environment.close();
            }
            logger.info("ONNX resources released");
        } catch (Exception e) {
            logger.error("Error closing ONNX resources", e);
        }
    }

    public EmbeddingResult embed(int[] inputIds, int[] attentionMask) {
        try {
            long[][] inputIdsBatch = new long[1][inputIds.length];
            long[][] attentionMaskBatch = new long[1][attentionMask.length];
            long[][] tokenTypeIdsBatch = new long[1][inputIds.length];

            for (int i = 0; i < inputIds.length; i++) {
                inputIdsBatch[0][i] = inputIds[i];
                attentionMaskBatch[0][i] = attentionMask[i];
                tokenTypeIdsBatch[0][i] = 0L; // todos 0
            }

            OnnxTensor inputIdsTensor = OnnxTensor.createTensor(environment, inputIdsBatch);
            OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(environment, attentionMaskBatch);
            OnnxTensor tokenTypeIdsTensor = OnnxTensor.createTensor(environment, tokenTypeIdsBatch);

            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input_ids", inputIdsTensor);
            inputs.put("attention_mask", attentionMaskTensor);
            inputs.put("token_type_ids", tokenTypeIdsTensor);

            OrtSession.Result result = session.run(inputs);

            float[] embeddings = extractEmbeddings(result);

            inputIdsTensor.close();
            attentionMaskTensor.close();
            tokenTypeIdsTensor.close();
            result.close();

            return new EmbeddingResult(Embedding.from(embeddings));

        } catch (Exception e) {
            logger.error("Error embedding tokenized input", e);
            throw new RuntimeException("Failed to embed tokenized input", e);
        }
    }



    public static class EmbeddingResult {
        private final Embedding content;

        public EmbeddingResult(Embedding content) {
            this.content = content;
        }

        public Embedding content() {
            return content;
        }
    }


}