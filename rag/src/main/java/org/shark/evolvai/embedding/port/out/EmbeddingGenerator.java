package org.shark.evolvai.embedding.port.out;

import java.util.List;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;

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
     * Genera un embedding a partir de vectores tokenizados manualmente.     *
     */
    Embedding generateEmbedding(int[] inputIds, int[] attentionMask);

    /**
     * Genera múltiples embeddings a partir de múltiples segmentos.
     */
    List<Embedding> generateEmbeddings(List<TextSegment> segments);
}
