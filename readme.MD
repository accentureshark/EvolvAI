# EvolvAI RAG

## Descripción

EvolvAI RAG es un sistema conversacional inteligente basado en RAG (Retrieval-Augmented Generation) desarrollado en Java con Spring Boot y LangChain4j. Su objetivo es brindar respuestas enriquecidas con contexto relevante para organizaciones, áreas o usuarios individuales, soportando múltiples perfiles y entidades jurídicas.

La arquitectura sigue el patrón hexagonal y se apoya exclusivamente en tecnologías libres y gratuitas, con un diseño modular que facilita la extensibilidad. Usa Docker o Podman indistintamente para levantar servicios.

---

## 🨠 Arquitectura

### Componentes principales

* **Admin**: Gestión de usuarios, roles, prompts, organizaciones y configuración personalizada.
* **Embedding**: Generación de vectores con ONNX y con Ollama, configurable, con persistencia en PgVector o en memoria. Búsqueda semántica eficiente.
* **LLM**: Integración con modelos de lenguaje (Llama vía LocalAI, OLLAMA o Bedrock).
* **Inference**: Orquestador del flujo completo RAG.
* **Chat Memory**: Manejo del contexto conversacional activo (memoria de corto plazo), con soporte en memoria o PostgreSQL.
* **Prompt Management**: Soporte para prompts personalizados asociados a usuarios, áreas u organizaciones.
* **Chat History**: Almacenamiento de logs de conversaciones, con orden cronológico y persistencia configurable.

---

## 🔁 Ejemplo de flujo RAG completo

1. **Usuario inicia conversación** (opcionalmente con un archivo PDF o texto).
2. El sistema:

    * Extrae texto y lo vectoriza vía ONNX u Ollama.
    * Guarda vectores en PgVector o en memoria.
    * Registra conversación en `ChatHistory`.
3. Se genera una respuesta con:

    * Recuperación semántica (`EmbeddingUseCase` → documentos relevantes).
    * Inserción en el prompt (`PromptService` + contexto).
    * LLM responde (LocalAI, Ollama o Bedrock).
4. La interacción se guarda en `ChatMemory` y `ChatHistory` para próximas consultas.

Este enfoque permite:

* Respuestas personalizadas por perfil u organización.
* Persistencia y trazabilidad completa.
* Razonamiento contextual y reutilización conversacional.

---

## 🚀 Tecnologías principales

* **Java** 17
* **Spring Boot** 3.4.1
* **LangChain4j** 0.37.0-SNAPSHOT
* **ONNX Runtime** 1.20.0
* **PostgreSQL + pgvector**
* **LocalAI / Ollama / Amazon Bedrock**
* **MapDB (memoria persistente opcional)**
* **Swagger (OpenAPI UI)**
* **Podman / Docker** (intercambiables)
* **JUnit 5 + Mockito** para testing

---

## ⚙️ Requisitos previos

* Java 17+
* Maven 3.8+
* Docker o Podman con Docker Compose o Podman Compose
* 8GB RAM mínimo (recomendado 16GB para LLMs)

---

## 🔪 Instalación y ejecución

```bash
# 1. Clonar el repositorio
$ git clone https://github.com/tu-usuario/evolvai-rag.git
$ cd evolvai-rag

# 2. Descargar modelos
# - ONNX: src/main/resources/model/all-MiniLM-L6-v2.onnx
# - GGUF: src/main/resources/model/ggml-model.gguf

# 3. Configurar LLM (ejemplo LocalAI)
# src/main/resources/model/llama2.yaml
name: llama2
backend: llama-cpp
parameters:
  model: ggml-model.gguf
  temperature: 0.7
  n_ctx: 1024

# 4. Iniciar servicios (usar uno de los dos)
$ podman-compose up -d  # o
$ docker-compose up -d

# 5. Ejecutar la app
$ mvn clean install
$ mvn spring-boot:run
```

---

## 🛠️ Configuración

Ubicada en `application.yaml`:

* Embeddings: ONNX / almacenamiento PgVector o memoria
* LLM: LocalAI, Ollama o AWS Bedrock
* Inferencia: Parámetros como max-results, min-score
* Persistencia: Chat memory (MapDB o PostgreSQL)
* Prompts: Asociación y configuración por usuario, área u organización

---

## 📂 Estructura del proyecto

```bash
src/
├── main/java/org/shark/evolvai/
│   ├── admin/               # Dominio administrativo
│   ├── embedding/           # Embeddings
│   ├── llm/                 # Proveedores de lenguaje
│   ├── inference/           # Orquestación RAG
│   ├── chathistory/         # Chat memory e historial
│   └── config/              # Configuraciones generales
├── resources/
│   ├── model/               # Modelos ONNX / GGUF
│   └── application.yaml     # Configuración global
└── test/                    # Pruebas unitarias e integración
```

---

## 🥐 Pruebas

```bash
# Todas las pruebas
mvn test

# Solo unitarias
mvn test -Dtest=*Test

# Solo integración
mvn test -Dtest=*IntegrationTest
```

---

## 🐳 Docker / Podman Compose

Servicios incluidos:

### `pgvector`

* PostgreSQL con extensión `pgvector`
* Persistencia para embeddings y memoria
* Script `init_pgvector.sql` incluido

### `localai`

* Inferencia LLM local compatible con OpenAI
* Modelos montados desde `resources/model`

```bash
# Iniciar
podman-compose up -d  # o docker-compose up -d

# Logs
podman-compose logs -f

# Apagar
podman-compose down  # o docker-compose down

# Limpiar todo
podman-compose down -v
```

---

## 🌐 Interfaz Web

Se incluye un frontend HTML (`chat.html`) con diseño estilo mensajería instantánea. Incluye:

* Carga de archivos
* Prompt personalizado
* Historial y memoria
* Log de consola

---

## 📎 Recursos adicionales

* Diagramas UML y ERD disponibles en la carpeta `docs`
* Scripts de carga en `admin/scripts`
* Presentación visual: ver `PlanCarrera.pdf`

---

## 👥 Autores y colaboración

Desarrollado por Shark IA Team 💈. Colaboraciones y issues son bienvenidos en [GitHub](https://github.com/tu-usuario/evolvai-rag).

---

## 📄 Licencia

Software libre bajo licencia MIT. ¡Usalo, modificalo y compartilo libremente!
