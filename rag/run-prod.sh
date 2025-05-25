#!/bin/bash

echo "🛠️ Compilando y levantando entorno completo (pgvector + ollama + app Spring Boot)..."
podman-compose -f docker-compose.base.yml -f docker-compose.prod.yml up --build
