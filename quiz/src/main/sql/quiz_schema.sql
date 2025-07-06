-- Crear esquema y tablas necesarias
CREATE SCHEMA IF NOT EXISTS quiz;

CREATE TABLE IF NOT EXISTS quiz.quiz_definition (
    id UUID PRIMARY KEY,
    document_id TEXT UNIQUE NOT NULL,
    tipo TEXT,
    tema TEXT,
    version TEXT,
    steps_json JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE IF NOT EXISTS quiz.quiz_response (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_document_id TEXT NOT NULL,
    usuario TEXT NOT NULL,
    respuestas JSONB NOT NULL,
    resultado_inferencia TEXT,
    created_at TIMESTAMP DEFAULT now()
);