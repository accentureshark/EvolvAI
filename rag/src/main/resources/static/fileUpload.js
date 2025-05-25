// Lógica de subida y gestión de archivos
import { log } from './utils.js';
import { loadUploadedFiles, BACKEND_URL } from './api.js';

export function setupFileUpload() {
    const fileInput = document.getElementById("file-input");
    const fileList = document.getElementById("file-list");
    const fileLabel = document.getElementById("file-label");
    const fileUploadPanel = document.getElementById("file-upload-panel");

    // Crear icono de disquete si no existe
    let saveIcon = document.getElementById("file-save-icon");
    if (!saveIcon) {
        saveIcon = document.createElement("span");
        saveIcon.id = "file-save-icon";
        saveIcon.title = "Guardar archivo en el backend";
        saveIcon.textContent = "💾";
        saveIcon.style.cursor = "pointer";
        saveIcon.style.marginLeft = "5px";
        saveIcon.style.display = "none";
        // Insertar el icono después del fileLabel en el panel de archivos
        if (fileUploadPanel) {
            fileUploadPanel.appendChild(saveIcon);
        }
    }

    // Mostrar el diálogo de archivos al hacer click en el clip
    if (fileLabel && fileInput) {
        fileLabel.onclick = function () {
            fileInput.click();
        };
    }

    fileInput.addEventListener("change", function () {
        fileList.innerHTML = "";
        for (const file of fileInput.files) {
            const li = document.createElement("li");
            li.textContent = file.name;
            fileList.appendChild(li);
        }
        log(`📎 Archivos cargados: ${fileInput.files.length}`);
        // Mostrar icono de guardar si hay archivos
        saveIcon.style.display = fileInput.files.length > 0 ? "inline-block" : "none";
    });

    saveIcon.onclick = function () {
        if (!fileInput.files.length) return;
        const file = fileInput.files[0];
        const formData = new FormData();
        formData.append("file", file);

        saveIcon.textContent = "⏳";
        // El endpoint correcto es /api/inference/upload
        fetch(`${BACKEND_URL}/api/inference/upload-document`, {
            method: "POST",
            body: formData
        })
        .then(async res => {
            let msg = await res.text();
            if (!res.ok) {
                log("❌ Backend respondió: " + msg);
                throw new Error(msg || "Error al subir el archivo");
            }
            return msg;
        })
        .then(() => {
            log(`✅ Archivo "${file.name}" subido correctamente`);
            saveIcon.textContent = "💾";
            fileInput.value = "";
            fileList.innerHTML = "";
            saveIcon.style.display = "none";
            loadUploadedFiles();
        })
        .catch(err => {
            log("❌ Error al subir archivo: " + err.message);
            saveIcon.textContent = "💾";
        });
    };
}

export { loadUploadedFiles };
