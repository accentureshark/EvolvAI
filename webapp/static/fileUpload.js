// fileUpload.js
import { log } from './utils.js';
import { BACKEND_URL } from './api.js';

// Variable global/exportada para saber cuál está seleccionado
export let selectedDocumentId = null;

// Función para marcar el archivo/documento seleccionado visualmente
function markSelectedDocument(li, docId) {
    document.querySelectorAll("#file-list li").forEach(node => node.classList.remove("selected"));
    li.classList.add("selected");
    selectedDocumentId = docId;
    log(`📝 Documento seleccionado para consultas: ${docId}`);
}

// Renderiza la lista de archivos/documentos subidos y habilita selección
export function populateFileList(files) {
    const fileList = document.getElementById("file-list");
    fileList.innerHTML = "";
    if (!files || !files.length) {
        selectedDocumentId = null;
        return;
    }
    files.forEach(docId => {
        const li = document.createElement("li");
        li.textContent = docId;
        li.title = "Usar este documento para consultas RAG";
        li.style.cursor = "pointer";
        li.onclick = () => markSelectedDocument(li, docId);
        fileList.appendChild(li);
    });
    // Si nada seleccionado, por defecto el primero
    if (!selectedDocumentId && fileList.children.length > 0) {
        markSelectedDocument(fileList.children[0], files[0]);
    }
}

// Carga la lista de archivos/documentos subidos desde el backend
export function loadUploadedFiles() {
    fetch(`${BACKEND_URL}/api/embeddings/documents`)
        .then(res => res.json())
        .then(files => populateFileList(files))
        .catch(err => log("❌ Error listando archivos: " + err.message));
}

export function setupFileUpload() {
    const fileInput = document.getElementById("file-input");
    const fileList = document.getElementById("file-list");
    const fileLabel = document.getElementById("file-label");
    const fileUploadPanel = document.getElementById("file-upload-panel");

    // Crear icono de borrado si no existe
    let deleteIcon = document.getElementById("file-delete-icon");
    if (!deleteIcon) {
        deleteIcon = document.createElement("span");
        deleteIcon.id = "file-delete-icon";
        deleteIcon.title = "Borrar todos los embeddings";
        deleteIcon.textContent = "🗑️";
        deleteIcon.style.cursor = "pointer";
        deleteIcon.style.marginRight = "5px";
        if (fileLabel && fileUploadPanel) {
            fileUploadPanel.insertBefore(deleteIcon, fileLabel);
        }
    }

    deleteIcon.onclick = function () {
        if (!confirm("¿Estás seguro que querés borrar todos los embeddings? Esta acción no se puede deshacer.")) {
            return;
        }
        deleteIcon.textContent = "⏳";
        fetch(`${BACKEND_URL}/api/embeddings/remove-all`, {
            method: "DELETE"
        })
            .then(async res => {
                const msg = await res.text();
                if (!res.ok) {
                    log("❌ Error al borrar: " + msg);
                    throw new Error(msg || "Error al eliminar embeddings");
                }
                log("🗑️ Todos los embeddings fueron eliminados.");
                deleteIcon.textContent = "🗑️";
                fileList.innerHTML = "";
                selectedDocumentId = null;
                loadUploadedFiles();
            })
            .catch(err => {
                log("❌ Error eliminando embeddings: " + err.message);
                deleteIcon.textContent = "🗑️";
            });
    };

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
        if (fileUploadPanel) {
            fileUploadPanel.appendChild(saveIcon);
        }
    }

    fileInput.addEventListener("change", function () {
        fileList.innerHTML = "";
        for (const file of fileInput.files) {
            const li = document.createElement("li");
            li.textContent = file.name;
            fileList.appendChild(li);
        }
        log(`📎 Archivos cargados: ${fileInput.files.length}`);
        saveIcon.style.display = fileInput.files.length > 0 ? "inline-block" : "none";
    });

    saveIcon.onclick = function () {
        if (!fileInput.files.length) return;
        const file = fileInput.files[0];
        const formData = new FormData();
        formData.append("file", file);

        saveIcon.textContent = "⏳";
        fetch(`${BACKEND_URL}/api/embeddings/upload`, {
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
                loadUploadedFiles(); // Esto recarga la lista y selecciona automáticamente el nuevo documento
            })
            .catch(err => {
                log("❌ Error al subir archivo: " + err.message);
                saveIcon.textContent = "💾";
            });
    };
}

// Exporta también para que otros módulos puedan refrescar archivos y saber el docId seleccionado
// export { loadUploadedFiles };
