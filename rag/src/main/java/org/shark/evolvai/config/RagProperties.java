package org.shark.evolvai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;
import java.util.Map;

/**
 * Central configuration class for the RAG system.
 * All properties are mapped from application.yaml under the prefix "rag".
 */
@Data
@ConfigurationProperties(prefix = "rag")
public class RagProperties {

    /** Embedding configuration: storage, chunking, generators, etc. */
    private Embedding embedding;

    /** LLM configuration: provider, prompt, temperature, etc. */
    private Llm llm;

    /** Inference parameters: max results, similarity thresholds. */
    private Inference inference;

    /** Metadata enrichment config for indexing and querying. */
    private Metadata metadata;

    /** List of available MCP servers to fetch contextual chunks from. */
    private List<Mcp> mcps;

    /**
     * Configuration block for external Metadata/Chunk Providers (MCP servers).
     */
    @Data
    public static class Mcp {

        /** Unique identifier for the MCP source (used as sourceId in RAG). */
        private String id;

        /** Base URL for the MCP server (e.g., http://localhost:8090). */
        private String baseUrl;

        /** Timeout in milliseconds for MCP calls. */
        private int timeoutMs;

        /** Default filtering parameters used if none are provided in the query. */
        private Map<String, String> defaultParams;
    }

    /**
     * Configuration for the embedding subsystem.
     */
    @Data
    public static class Embedding {

        private Chunking chunking;
        private Storage storage;
        private Generator generator;
        private Pgvector pgvector;
        private Onnx onnx;
        private Ollama ollama;
        private Tokenizer tokenizer;

        /**
         * Configuration for text chunking strategy.
         */
        @Data
        public static class Chunking {
            /** Maximum number of words per chunk. */
            private int size;

            /** Number of overlapping words between chunks. */
            private int overlap;

            /** Metadata keys to include in each chunk. */
            private List<String> enrichWith;
        }

        /**
         * Type of embedding storage backend (e.g., "pgVector").
         */
        @Data
        public static class Storage {
            private String type;
        }

        /**
         * Configuration for the embedding vector generator.
         */
        @Data
        public static class Generator {
            /** Provider name (e.g., "ollama", "onnx"). */
            private String provider;

            /** Model name used by the provider. */
            private String model;
        }

        /**
         * PgVector-specific settings.
         */
        @Data
        public static class Pgvector {
            private String host;
            private int port;
            private String database;
            private String user;
            private String password;
            private String tableName;
            private int dimensions;
        }

        /**
         * Configuration for ONNX-based local embedding generation.
         */
        @Data
        public static class Onnx {
            /** Path to ONNX model file (e.g., classpath or absolute path). */
            private String modelPath;
        }

        /**
         * Configuration for embedding generation via Ollama backend.
         */
        @Data
        public static class Ollama {
            private String baseUrl;
            private String model;
        }

        /**
         * Tokenizer configuration for chunk processing or prompt building.
         */
        @Data
        public static class Tokenizer {
            private String path;
            private Generator generator;

            @Data
            public static class Generator {
                private String provider;
            }
        }
    }

    /**
     * Configuration for the LLM (Large Language Model) subsystem.
     */
    @Data
    public static class Llm {
        private String provider;
        private String prompt;
        private Ollama ollama;
        private Llama llama;

        /**
         * Settings for Ollama-backed LLMs.
         */
        @Data
        public static class Ollama {
            private String baseUrl;
            private String model;
            private double temperature;
            private int timeoutSec;
        }

        /**
         * Settings for Llama or similar API-based LLMs.
         */
        @Data
        public static class Llama {
            private String baseUrl;
            private String apiKey;
            private String model;
            private double temperature;
            private int maxTokens;
            private double topP;
            private int topK;
        }
    }

    /**
     * Inference-related configuration such as score thresholds and limits.
     */
    @Data
    public static class Inference {
        private int maxResults;
        private double minScore;
    }

    /**
     * Metadata enrichment for document processing and responses.
     */
    @Data
    public static class Metadata {
        /** Metadata keys to be used in context or indexing. */
        private List<String> enrichWith;
    }
}
