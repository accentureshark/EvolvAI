const BACKEND_URL = "http://localhost:8081";

// --- Funciones auxiliares ---

function log(msg) {
    const logEntries = document.getElementById("log-entries");
    logEntries.innerHTML += "\n" + msg;
    logEntries.scrollTop = logEntries.scrollHeight;
}

function showSpinner(show) {
    let spinner = document.getElementById("spinner");
    if (!spinner) {
        spinner = document.createElement("div");
        spinner.id = "spinner";
        spinner.className = "spinner";
        spinner.innerHTML = `<img src="logo.png" alt="Cargando..." class="spinner-logo">`;
        document.getElementById("chat-area").prepend(spinner);
    }
    spinner.style.display = show ? "block" : "none";
}

// --- Funciones principales ---

function setupFileUpload() {
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

function loadUploadedFiles() {
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

function addMessage(text, who) {
    const container = document.getElementById("messages");
    const msg = document.createElement("div");
    msg.className = "message " + who;
    if (who === "bot") {
        msg.innerHTML = `<img class="avatar" src="shark-bot.png" alt="Bot"><div class="text">${text}</div>`;
    } else {
        msg.innerHTML = `<div class="text">${text}</div>`;
    }
    container.appendChild(msg);
    container.scrollTop = container.scrollHeight;
}

async function handleFormSubmit(e) {
    e.preventDefault();
    const fileInput = document.getElementById("file-input");
    const userInput = document.getElementById("user-input");
    const file = fileInput.files[0];
    const text = userInput.value.trim();

    if (file) {
        showSpinner(true);
        log('Subiendo archivo: ' + file.name);
        const formData = new FormData();
        formData.append('file', file);
        try {
            const res = await fetch(`${BACKEND_URL}/api/inference/upload-document`, {
                method: 'POST',
                body: formData
            });
            if (res.ok) {
                log('Archivo subido y procesado: ' + file.name);
                loadUploadedFiles();
            } else {
                log('Error al subir el archivo: ' + file.name);
            }
        } catch {
            log('Error de red al subir el archivo: ' + file.name);
        }
        fileInput.value = '';
        showSpinner(false);
    } else if (text) {
        addMessage(text, 'user');
        userInput.value = '';
        showSpinner(true);
        log('Enviando consulta: ' + text);
        try {
            let endpoint, body;
            endpoint = `${BACKEND_URL}/api/embeddings/search`;
            body = `query=${encodeURIComponent(text)}&maxResults=5&minScore=0.7`;
            endpoint += `?${body}`;
            const res = await fetch(endpoint, { method: 'GET' });
            if (res.ok) {
                const data = await res.json();
                let respuesta = Array.isArray(data) ? data.join('\n') : JSON.stringify(data);
                addMessage(respuesta, 'bot');
                log('Respuesta recibida del backend.');
            } else {
                addMessage('Error en la respuesta del backend.', 'bot');
                log('Error en la respuesta del backend.');
            }
        } catch {
            addMessage('Error de red al consultar.', 'bot');
            log('Error de red al consultar.');
        }
        showSpinner(false);
    }
}

// --- Inicialización ---

document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("input-area").addEventListener("submit", handleFormSubmit);
    setupFileUpload();
    loadUploadedFiles();
    // Mostrar mensaje del bot si no hay mensajes
    const messages = document.getElementById("messages");
   // if (messages.children.length === 0) {
   //     addMessage("¿En qué te puedo ayudar pequeño Sharkcamonte?", "bot");
   // }
});