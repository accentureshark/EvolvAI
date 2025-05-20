package org.shark.evolvai.embedding.adapter.tokenizer;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;

import java.io.IOException;
import java.util.Arrays;

public class OnnxCompatibleTokenizer {

    private final HuggingFaceTokenizer tokenizer;

    public OnnxCompatibleTokenizer(String tokenizerPath) throws IOException {
        this.tokenizer = HuggingFaceTokenizer.newInstance(tokenizerPath);
    }

    public int[] getInputIds(String text) {
        long[] longIds = tokenizer.encode(text).getIds();
        return convertToIntArray(longIds);
    }

    public int[] getAttentionMask(String text) {
        long[] longMask = tokenizer.encode(text).getAttentionMask();
        return convertToIntArray(longMask);
    }

    private int[] convertToIntArray(long[] longArray) {
        return Arrays.stream(longArray).mapToInt(l -> (int) l).toArray();
    }

    public void debug(String text) {
        var encoding = tokenizer.encode(text);
        System.out.println("Text: " + text);
        System.out.println("Input IDs: " + Arrays.toString(convertToIntArray(encoding.getIds())));
        System.out.println("Attention Mask: " + Arrays.toString(convertToIntArray(encoding.getAttentionMask())));
        System.out.println("Tokens: " + encoding.getTokens());
    }
}
