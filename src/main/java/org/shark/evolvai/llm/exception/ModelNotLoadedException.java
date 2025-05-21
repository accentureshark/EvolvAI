
package org.shark.evolvai.llm.exception;

public class ModelNotLoadedException extends RuntimeException {
    public ModelNotLoadedException(String modelName) {
        super("El modelo Ollama '" + modelName + "' no está cargado. Por favor, cárgalo antes de consultar.");
    }
}