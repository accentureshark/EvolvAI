package org.shark.evolvai.embedding.adapter.output.generator;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import org.shark.evolvai.embedding.port.output.EmbeddingGenerator;
import org.shark.evolvai.embedding.provider.ONNXProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Qualifier("onnxEmbeddingGenerator")
public class ONNXEmbeddingGenerator implements EmbeddingGenerator {

    private final ONNXProvider onnxProvider;

    public ONNXEmbeddingGenerator(ONNXProvider onnxProvider) {
        this.onnxProvider = onnxProvider;
    }

    @Override
    public Embedding generateEmbedding(String text) {
        return onnxProvider.embed(text).content();
    }

    @Override
    public Embedding generateEmbedding(TextSegment segment) {
        return onnxProvider.embed(segment.text()).content();
    }

    @Override
    public List<Embedding> generateEmbeddings(List<TextSegment> texts) {
        return onnxProvider.embedAll(texts).content();
    }

    @Override
    public Embedding generateEmbedding(int[] inputIds, int[] attentionMask) {
        return onnxProvider.embed(inputIds, attentionMask).content();
    }
}
