version: '3.8'

services:
  pgvector:
    image: ankane/pgvector
    container_name: evolvai_pgvector
    restart: always
    environment:
      POSTGRES_DB: evolvai
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres1234
    ports:
      - "5432:5432"
    volumes:
      - ./src/main/sql/initdb:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD", "pg_isready", "-U", "postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  ollama:
    image: ollama/ollama:latest
    container_name: evolvai_ollama
    ports:
      - "11434:11434"
    environment:
      - OLLAMA_MODELS=phi3:mini
      - OLLAMA_DEBUG=INFO
    volumes:
      - ollama-data:/root/.ollama
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:11434"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: .
      dockerfile: Dockerfile.backend
    container_name: evolvai_backend
    ports:
      - "8081:8080"
    depends_on:
      - pgvector
      - ollama

  webapp:
    image: nginx:alpine
    container_name: evolvai_web
    ports:
      - "8080:80"
    volumes:
      - ./webapp:/usr/share/nginx/html:ro
    depends_on:
      - backend

volumes:
  ollama-data:
