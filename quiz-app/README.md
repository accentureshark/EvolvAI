# Quiz App Frontend

![React](https://img.shields.io/badge/react-%2320232a.svg?style=for-the-badge&logo=react&logoColor=%2361DAFB)
![Vite](https://img.shields.io/badge/vite-%23646CFF.svg?style=for-the-badge&logo=vite&logoColor=white)
![PrimeReact](https://img.shields.io/badge/PrimeReact-10B981?style=for-the-badge&logo=primereact&logoColor=white)
![TailwindCSS](https://img.shields.io/badge/tailwindcss-%2338B2AC.svg?style=for-the-badge&logo=tailwind-css&logoColor=white)

## 📚 Resumen

Este es el frontend de la Quiz App, una aplicación web construida con React. Proporciona una interfaz intuitiva para responder cuestionarios y, para los administradores, un panel para crearlos y gestionarlos.

La aplicación cuenta con un sistema basado en roles:
- **Rol de Administrador**: Permite a los usuarios crear nuevos cuestionarios, añadir preguntas y gestionar los existentes.
- **Rol de Usuario**: Permite a los usuarios navegar y responder los cuestionarios disponibles.

Este proyecto está desarrollado para funcionar con su correspondiente [Backend en Spring Boot](link-to-your-backend-repo). *(Nota: Necesitarás actualizar este enlace)*

## ✨ Características

-   **Autenticación de Usuarios**: Sistema de inicio de sesión seguro.
-   **Control de Acceso Basado en Roles**: Vistas y funcionalidades separadas para administradores y usuarios.
-   **Creación de Cuestionarios**: Los administradores pueden crear cuestionarios con múltiples preguntas y respuestas.
-   **Resolución de Cuestionarios**: Los usuarios pueden seleccionar un cuestionario y responder a las preguntas.
-   **Diseño Adaptable**: Una interfaz de usuario limpia y moderna que funciona en diferentes tamaños de pantalla, construida con PrimeReact y Tailwind CSS.

## 🛠️ Tecnologías Utilizadas

-   **Framework**: [React](https://reactjs.org/)
-   **Herramienta de Construcción**: [Vite](https://vitejs.dev/)
-   **Librería de UI**: [PrimeReact](https://primereact.org/)
-   **Estilos**: [Tailwind CSS](https://tailwindcss.com/)
-   **Enrutamiento**: [React Router DOM](https://reactrouter.com/)
-   **Gestión de Estado/Datos**: [TanStack React Query](https://tanstack.com/query/latest)
-   **Lenguaje**: JavaScript/TypeScript

## 🚀 Cómo Empezar

Sigue estas instrucciones para obtener una copia del proyecto y ejecutarla en tu máquina local para desarrollo y pruebas.

### Prerrequisitos

Asegúrate de tener instalado lo siguiente:
-   [Node.js](https://nodejs.org/) (v18 o superior recomendado)
-   [npm](https://www.npmjs.com/) o cualquier otro gestor de paquetes como [yarn](https://yarnpkg.com/) o [bun](https://bun.sh/).

### Instalación

1.  **Clona el repositorio:**
    ```bash
    git clone https://your-repo-url.git
    cd quiz-app
    ```

2.  **Instala las dependencias:**
    ```bash
    npm install
    ```
    *(O `yarn install`, `bun install`)*

### Ejecutar la Aplicación

Para iniciar el servidor de desarrollo, ejecuta:
```bash
npm run dev
```
La aplicación estará disponible en `http://localhost:5173` (u otro puerto si el 5173 está ocupado).

## 📜 Scripts Disponibles

En el directorio del proyecto, puedes ejecutar los siguientes comandos:

-   `npm run dev`: Ejecuta la aplicación en modo de desarrollo.
-   `npm run build`: Compila la aplicación para producción en la carpeta `dist`.
-   `npm run lint`: Analiza el código base con ESLint.
-   `npm run preview`: Sirve la compilación de producción localmente para previsualizarla.

## 🔗 Conexión con el Backend

Este frontend está diseñado para comunicarse con una API de backend. Asegúrate de que el servidor del backend se esté ejecutando y que el endpoint de la API esté correctamente configurado en la aplicación frontend.
