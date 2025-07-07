
# Proyecto Quiz AI

## 1. Propósito del Proyecto

Este proyecto, denominado "Quiz AI", es una aplicación backend diseñada para gestionar y evaluar cuestionarios interactivos. Su característica principal es la capacidad de procesar las respuestas de los usuarios y utilizar un motor de inferencia externo para obtener resultados o conclusiones basadas en dichas respuestas.

La aplicación permite definir cuestionarios dinámicos en formato JSON, almacenarlos, y luego servirlos a los usuarios. Una vez que un usuario completa un cuestionario, sus respuestas son enviadas a un servicio de inferencia que devuelve un resultado.

## 2. Arquitectura

El proyecto sigue una **Arquitectura Hexagonal** (también conocida como Puertos y Adaptadores), lo que le permite desacoplar el núcleo de la lógica de negocio de los detalles de la infraestructura (como la base de datos, APIs externas o la interfaz de usuario).

La estructura se organiza de la siguiente manera:

*   **Dominio (`domain`):** Contiene la lógica de negocio y las entidades principales.
    *   `model`: Define los objetos del dominio como `QuizDefinition` (la estructura de un cuestionario) y `QuizResponseRequest` (las respuestas de un usuario).
    *   `port/out`: Define las interfaces (puertos) que necesita el dominio para comunicarse con el exterior, como `QuizRepository` (para persistencia) e `InferenceClient` (para la inferencia).

*   **Aplicación (`application`):** Orquesta el flujo de datos y contiene los casos de uso.
    *   `service/QuizService`: Es el corazón de la aplicación. Utiliza los puertos de salida para implementar la lógica de negocio, como guardar una definición de quiz o procesar una respuesta.

*   **Adaptadores (`adapter`):** Son las implementaciones concretas de los puertos.
    *   `in/rest/QuizController`: Es un **adaptador de entrada** que expone la funcionalidad de la aplicación a través de una API REST. Recibe peticiones HTTP y llama al `QuizService`.
    *   `infrastructure`: Contiene los **adaptadores de salida**. Aunque no se muestra la implementación de `QuizRepository`, se infiere que interactúa con una base de datos. `InferenceClientImpl` sería la implementación que llama al servicio de IA.

## 3. Archivos Importantes

*   `pom.xml`: Define las dependencias del proyecto, como Spring Boot (para el servidor web), Spring Data JPA (para la base de datos), PostgreSQL (como motor de base de datos), y `springdoc-openapi` (para la documentación de la API con Swagger). También incluye una dependencia al módulo `rag`, lo que sugiere que el motor de inferencia podría estar relacionado con un sistema de Retrieval-Augmented Generation.

*   `application.yml`: Archivo de configuración de Spring Boot. Define el puerto del servidor (8082), la configuración de la conexión a la base de datos PostgreSQL, y los niveles de logging para depuración.

*   `QuizController.java`: El punto de entrada de la API REST. Define los siguientes endpoints:
    *   `POST /api/quiz/upload`: Para subir y guardar la definición de un nuevo quiz.
    *   `GET /api/quiz/{documentId}`: Para obtener la definición de un quiz existente.
    *   `POST /api/quiz/response`: Para enviar las respuestas de un usuario y obtener un resultado del motor de inferencia.
    *   `GET /api/quiz/list`: Para listar todos los quizzes disponibles.
    *   `GET /api/quiz/step`: Para obtener un paso específico de un quiz.

*   `QuizService.java`: Contiene la lógica de negocio principal. Coordina las operaciones entre la API REST y los sistemas externos (base de datos y motor de inferencia).

*   `InferenceClient.java`: Interfaz que define el contrato para comunicarse con el motor de inferencia. Esto permite cambiar la implementación del motor de inferencia sin afectar al resto de la aplicación.

*   `quiz_schema.sql`: Define el esquema de la base de datos. Contiene dos tablas principales:
    *   `quiz.quiz_definition`: Almacena las definiciones de los quizzes, incluyendo los pasos en formato JSON.
    *   `quiz.quiz_response`: Almacena las respuestas de los usuarios a un quiz, junto con el resultado de la inferencia.

## 4. Funcionalidad Principal

1.  **Definición de Quizzes**: Un administrador o sistema externo puede subir una definición de quiz en formato JSON a través del endpoint `/api/quiz/upload`. Esta definición se guarda en la base de datos.

2.  **Realización de un Quiz**: Un cliente (como una aplicación web o móvil) puede obtener la lista de quizzes y sus definiciones. El usuario responde a las preguntas del quiz paso a paso.

3.  **Procesamiento de Respuestas**: Una vez completado el quiz, el cliente envía todas las respuestas al endpoint `/api/quiz/response`.

4.  **Inferencia de Resultados**: El `QuizService` recibe las respuestas y, en lugar de evaluarlas con una lógica predefinida, las envía al `InferenceClient`. Este cliente se comunica con un servicio de IA externo que devuelve un resultado "inteligente" o una conclusión.

5.  **Almacenamiento**: Las respuestas y el resultado de la inferencia se guardan en la base de datos para futuros análisis.

En resumen, **Quiz AI es un servicio para crear y realizar cuestionarios, cuya principal característica es que la evaluación de los resultados no es determinista, sino que se delega a un sistema de inteligencia artificial.**
