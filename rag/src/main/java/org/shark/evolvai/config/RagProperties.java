package org.shark.evolvai.config;

import java.util.List;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Centraliza todas las propiedades de configuración bajo el prefijo "rag".
 * Esta clase se mapea automáticamente a partir de
 * application.yaml gracias a @ConfigurationProperties.
 */
@Data
@ConfigurationProperties(prefix = "rag")
public class RagProperties {

    /**
     * Configuración relacionada al módulo de embedding.
     * Incluye subcomponentes como chunking, storage, generator, modelos ONNX, Ollama, etc.
     */
    private Embedding embedding;

    /**
     * Configuración para el proveedor LLM (Large Language Model).
     * Permite elegir entre distintos backends (Ollama, Llama)
     * y ajustar parámetros como temperatura.
     */
    private Llm llm;

    /**
     * Parámetros para la fase de inferencia de RAG.
     * Permite definir cuántos resultados buscar y el umbral mínimo de puntaje.
     */
    private Inference inference;

    /**
     * Configuración de metadata adicional a enriquecer en cada documento.
     */
    private Metadata metadata;

    /**
     * Agrupa todas las propiedades relacionadas al proceso de embedding.
     */
    @Data
    public static class Embedding {

        /**
         * Parámetros para dividir texto en fragmentos (chunks).
         * - size: cantidad máxima de palabras por chunk.
         * - overlap: solapamiento en palabras entre chunks consecutivos.
         * - enrichWith: lista de claves de metadata que se agregarán a cada fragmento.
         */
        private Chunking chunking;

        /**
         * Tipo de almacenamiento de embeddings (e.g., "pgVector").
         * Define qué implementación usar para persistir vectores.
         */
        private Storage storage;

        /**
         * Configuración del generador de embeddings.
         * - provider: nombre del backend que genera vectores (e.g., "ollama").
         * - model: nombre del modelo de embeddings a utilizar.
         */
        private Generator generator;

        /**
         * Parámetros de conexión para pgVector(almacenamiento en PostgreSQL con extensión pgvector)
         * Incluye host, puerto, credenciales, nombre de tabla y dimensión del vector.
         */
        private Pgvector pgvector;

        /**
         * Ruta al modelo ONNX para generar embeddings localmente.
         */
        private Onnx onnx;

        /**
         * Configuración específica para usar Ollama como generador de embeddings.
         * Incluye baseUrl y nombre de modelo.
         */
        private Ollama ollama;

        /**
         * Configuración del tokenizador usado en RAG.
         * - path: ruta al archivo del tokenizador (puede ser classpath).
         * - generator.provider: proveedor de tokenizador (p.ej., "ollama").
         */
        private Tokenizer tokenizer;

        /**
         * Detalles para realizar el chunking de texto.
         */
        @Data
        public static class Chunking {
            /**
             * Tamaño máximo (en número de palabras) de cada fragmento.
             */
            private int size;

            /**
             * Cantidad de palabras que se solapan entre fragmentos consecutivos.
             */
            private int overlap;

            /**
             * Claves de metadata que se agregarán a cada fragmento.
             * Por ejemplo, "nivel" o "area" para enriquecer cada segmento.
             */
            private List<String> enrichWith;
        }

        /**
         * Configuración del tipo de almacenamiento de vectores.
         */
        @Data
        public static class Storage {
            /**
             * Identificador del tipo de almacenamiento (e.g., "pgVector").
             */
            private String type;
        }

        /**
         * Generador de embeddings: define proveedor y modelo.
         */
        @Data
        public static class Generator {
            /**
             * Nombre del proveedor de embeddings (e.g., "ollama").
             */
            private String provider;

            /**
             * Nombre del modelo de embeddings dentro del proveedor.
             */
            private String model;
        }

        /**
         * Conexión para almacenamiento en PGVector (PostgreSQL + extensión pgvector).
         */
        @Data
        public static class Pgvector {
            private String host;     // Dirección del servidor PostgreSQL
            private int port;        // Puerto de conexión a la base de datos
            private String database; // Nombre de la base de datos
            private String user;     // Usuario de la base de datos
            private String password; // Contraseña del usuario
            private String tableName; // Tabla donde se guardan los vectores (schema.tabla)
            private int dimensions;  // Dimensión de los vectores (debe coincidir con el modelo)
        }

        /**
         * Ruta al modelo ONNX para generar embeddings sin servicio externo.
         */
        @Data
        public static class Onnx {
            /**
             * Ruta del archivo ONNX (puede ser "classpath:..." o ruta absoluta).
             */
            private String modelPath;
        }

        /**
         * Configuración para el backend Ollama de embeddings.
         */
        @Data
        public static class Ollama {
            /**
             * URL base donde corre el servicio de Ollama (http://localhost:11434 por defecto).
             */
            private String baseUrl;

            /**
             * Nombre del modelo de Ollama que genera vectores.
             */
            private String model;
        }

        /**
         * Configuración del tokenizador.
         */
        @Data
        public static class Tokenizer {
            /**
             * Ubicación del archivo de tokenizador ("classpath:..." o ruta absoluta).
             */
            private String path;

            /**
             * Configuración del proveedor de tokenizador asociada a este tokenizador.
             */
            private Generator generator;

            @Data
            public static class Generator {
                /**
                 * Nombre del proveedor de tokenizador (e.g., "ollama").
                 */
                private String provider;
            }
        }
    }

    /**
     * Configuración para la parte de LLM (Large Language Model).
     */
    @Data
    public static class Llm {
        /**
         * Nombre del proveedor de LLM (por ejemplo, "ollama" o "llama").
         */
        private String provider;

        /**
         * Prompt base que se enviará al LLM para realizar consultas.
         */
        private String prompt;

        /**
         * Configuración específica para usar Ollama como LLM.
         */
        private Ollama ollama;

        /**
         * Configuración específica para usar Llama como LLM.
         */
        private Llama llama;

        @Data
        public static class Ollama {
            /**
             * URL base para el servicio de Ollama.
             */
            private String baseUrl;

            /**
             * Nombre del modelo de Ollama.
             */
            private String model;

            /**
             * Temperatura de generación (0.0 para determinista).
             */
            private double temperature;

            /**
             * Tiempo máximo de espera en segundos.
             */
            private int timeoutSec;
        }

        @Data
        public static class Llama {
            /**
             * URL base para el endpoint de Llama (servicio local o remoto).
             */
            private String baseUrl;

            /**
             * Clave de API si el servicio de Llama lo requiere.
             */
            private String apiKey;

            /**
             * Nombre del modelo de Llama.
             */
            private String model;

            /**
             * Temperatura para la generación de texto.
             */
            private double temperature;

            /**
             * Máximo de tokens a generar.
             */
            private int maxTokens;

            /**
             * Probabilidad de núcleo (top-p) para muestreo.
             */
            private double topP;

            /**
             * Número de tokens del top-k para muestreo.
             */
            private int topK;
        }
    }

    /**
     * Parámetros para la etapa de inferencia en RAG.
     */
    @Data
    public static class Inference {
        /**
         * Número máximo de resultados a recuperar.
         */
        private int maxResults;

        /**
         * Puntaje mínimo que debe tener un resultado para considerarse.
         */
        private double minScore;
    }

    /**
     * Claves de metadata adicionales para enriquecer las respuestas.
     */
    @Data
    public static class Metadata {
        /**
         * Lista de claves de metadata (e.g., "nivel", "area") que se usarán.
         */
        private List<String> enrichWith;
    }
}
