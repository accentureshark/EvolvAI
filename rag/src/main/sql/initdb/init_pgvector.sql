-- Enable pgvector
CREATE EXTENSION IF NOT EXISTS vector;

-- Schemas for structured separation
CREATE SCHEMA IF NOT EXISTS embedding;
CREATE SCHEMA IF NOT EXISTS admin;

-- Main embeddings table
CREATE TABLE IF NOT EXISTS embedding.embeddings
(
    embedding_id UUID PRIMARY KEY,
    embedding VECTOR(1024) NOT NULL,
    document_id VARCHAR(512),
    text TEXT NOT NULL,
    metadata JSONB
    );

CREATE INDEX IF NOT EXISTS embeddings_vector_idx
    ON embedding.embeddings USING ivfflat (embedding vector_l2_ops);