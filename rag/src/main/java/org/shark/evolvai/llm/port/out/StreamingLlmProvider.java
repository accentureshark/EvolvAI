package org.shark.evolvai.llm.port.out;

import dev.langchain4j.data.message.ChatMessage;
import reactor.core.publisher.Flux;

import java.util.List;

public interface StreamingLlmProvider {

    Flux<String> streamResponse(List<ChatMessage> context, String prompt);
}
