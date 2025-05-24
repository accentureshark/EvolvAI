// Lógica de subida y gestión de archivos
import { log } from './utils.js';
import { loadUploadedFiles, BACKEND_URL } from './api.js';

export function setupFileUpload() {
    const fileInput = document.getElementById("file-input");
    fileInput.addEventListener("change", function () {
        const fileList = document.getElementById("file-list");
        fileList.innerHTML = "";
        for (const file of fileInput.files) {
            const li = document.createElement("li");
            li.textContent = file.name;
            fileList.appendChild(li);
        }
        log(`📎 Archivos cargados: ${fileInput.files.length}`);
    });
}

export { loadUploadedFiles };

