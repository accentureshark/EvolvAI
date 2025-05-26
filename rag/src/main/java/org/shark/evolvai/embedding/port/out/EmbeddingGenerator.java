package org.shark.evolvai.embedding.port.out;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;

import java.util.List;

public interface EmbeddingGenerator {

    /**
     * Genera un embedding a partir de un texto en crudo.
     */
    Embedding generateEmbedding(String text);

    /**
     * Genera un embedding a partir de un segmento de texto (LangChain4j).
     */
    Embedding generateEmbedding(TextSegment segment);

    /**
     * Genera múltiples embeddings a partir de múltiples segmentos.
     */
    List<Embedding> generateEmbeddings(List<TextSegment> segments);

    /**
     * Genera un embedding a partir de vectores tokenizados manualmente
     * compatibles con modelos ONNX (input_ids y attention_mask).
     */
    Embedding generateEmbedding(int[] inputIds, int[] attentionMask);
}
