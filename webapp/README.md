# Webapp Static

Este directorio contiene los archivos estáticos de la interfaz web de EvolvAI. Aquí se encuentran los scripts JavaScript, hojas de estilo y otros recursos necesarios para la interacción del usuario con el backend.

## Estructura

- **api.js**  
  Funciones para interactuar con la API REST del backend (carga de archivos, prompts, etc).

- **prompt.js**  
  Lógica para cargar, mostrar y alternar el prompt utilizado por el modelo LLM.

- **utils.js**  
  Funciones utilitarias, como el registro de logs en la interfaz.

- **index.html**  
  Archivo HTML principal de la webapp.

- **styles.css**  
  Hojas de estilo para la apariencia de la webapp.

## Instalación y uso

1. Asegúrate de tener el backend corriendo en `http://localhost:8081`.
2. Sirve este directorio con un servidor web simple (por ejemplo, usando `python3 -m http.server` o similar).
3. Accede a `index.html` desde tu navegador.

## Funcionalidades principales

- Visualización y carga de archivos procesados por el backend.
- Visualización y edición del prompt utilizado por el modelo LLM.
- Interacción simple y directa con el backend vía fetch API.

## Notas

- El backend debe estar configurado correctamente y accesible desde la webapp.
- Las rutas y puertos pueden modificarse en `api.js` si es necesario.

---