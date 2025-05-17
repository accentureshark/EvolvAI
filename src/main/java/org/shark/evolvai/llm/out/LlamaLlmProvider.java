package org.shark.evolvai.llm.adapter.out;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.openai.OpenAiChatModel; // Cambia por Llama si tienes un wrapper
import org.shark.evolvai.llm.port.out.LlmProvider;
import org.springframework.stereotype.Component;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import java.util.List;

@Component
public class LlamaLlmProvider implements LlmProvider {

    private final OpenAiChatModel llamaModel; // Cambia por el modelo Llama real

    public LlamaLlmProvider(OpenAiChatModel llamaModel) {
        this.llamaModel = llamaModel;
    }

    @Override
    public ChatMessage send(List<ChatMessage> messages) {
        // Obtiene el mensaje AI de la respuesta y lo retorna como ChatMessage
        return llamaModel.generate(messages).content();
    }
    @Override
    public String generateResponse(String context, String query) {
        // Crear mensajes para el modelo usando el contexto y la consulta
        List<ChatMessage> messages = List.of(
                new SystemMessage("Usa esta información para responder a la pregunta del usuario: " + context),
                new UserMessage(query)
        );

        // Utilizar el mismo modelo que ya está configurado en esta clase
        ChatMessage response = llamaModel.generate(messages).content();

        // Devolver el texto de la respuesta
        return response.text();
    }

}