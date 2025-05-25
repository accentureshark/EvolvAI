#!/bin/bash

# Default values
STRUCTURE_JSON=""
OUTPUT_JSON=""

# Parse named args
while [[ "$#" -gt 0 ]]; do
    case $1 in
        --input) INPUT="$2"; shift ;;
        --structure) STRUCTURE_JSON="$2"; shift ;;
        --output) OUTPUT_JSON="$2"; shift ;;
        *) echo "Parámetro desconocido: $1" >&2; exit 1 ;;
    esac
    shift
done

# Validación básica
if [[ -z "$INPUT" ]]; then
    echo "Uso: $0 --input archivo.pdf|.txt [--structure estructura.json] [--output salida.json]"
    exit 1
fi

# Inferencia de nombres si no se pasan
BASENAME=$(basename "$INPUT")
BASENAME_NOEXT="${BASENAME%.*}"
STRUCTURE_JSON=${STRUCTURE_JSON:-"${BASENAME_NOEXT}-structure.json"}
OUTPUT_JSON=${OUTPUT_JSON:-"${BASENAME_NOEXT}-data.json"}

echo "📄 Entrada: $INPUT"
echo "🧠 Estructura: $STRUCTURE_JSON"
echo "📝 Salida: $OUTPUT_JSON"

# Ejecutar Java con fat JAR
java -jar target/pre-parser-0.1-SNAPSHOT.jar "$INPUT" "$STRUCTURE_JSON" "$OUTPUT_JSON"
