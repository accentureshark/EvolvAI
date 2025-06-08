
import { log } from './utils.js';

export const BACKEND_URL = "http://localhost:8081";
//export const BACKEND_URL = "";


export function loadUploadedFiles() {
    fetch(`${BACKEND_URL}/api/embeddings/documents`)
        .then((res) => {
            if (!res.ok) throw new Error("No se pudo obtener la lista de archivos");
            return res.json();
        })
        .then((files) => {
            const fileList = document.getElementById("file-list");
            fileList.innerHTML = "";
            files.forEach((fileName) => {
                const li = document.createElement("li");
                li.textContent = fileName;
                fileList.appendChild(li);
            });
            log(`📂 Se cargaron ${files.length} archivo(s) desde el backend`);
        })
        .catch((err) => {
            log("❌ Error al cargar archivos: " + err.message);
        });
}

export function fetchDefaultPrompt() {
    return fetch(`${BACKEND_URL}/api/llm/prompt`)
        .then(res => res.ok ? res.text() : Promise.reject("No se pudo obtener el prompt por defecto"));
}

