#!/bin/sh
ollama serve &
OLLAMA_PID=$!

until curl -s http://localhost:11434; do
  echo "Esperando a que Ollama server esté listo..."
  sleep 2
done

check_and_pull() {
  MODEL_NAME=$1
  if [ ! -f "/root/.ollama/models/$MODEL_NAME" ]; then
    echo "Descargando modelo: $MODEL_NAME"
    curl -X POST http://localhost:11434/api/pull -d "{\"name\": \"$MODEL_NAME\"}" -H 'Content-Type: application/json'
  else
    echo "El modelo $MODEL_NAME ya existe, omitiendo descarga."
  fi
}

source ./rag/.env

check_and_pull "$OLLAMA_LLM_MODEL"
check_and_pull "$OLLAMA_EMBEDDING_MODEL"

kill $OLLAMA_PID
wait $OLLAMA_PID
exec ollama serve
