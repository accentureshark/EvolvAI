package org.shark.evolvai.llm.provider;

import dev.langchain4j.model.input.Prompt;

public interface LLMProvider {
    String getCompletion(Prompt prompt);

    String getProviderName();
}

