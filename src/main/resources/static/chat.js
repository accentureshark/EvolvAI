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

    // Lógica de truncado
    const maxLen = 200;
    let isTruncated = text.length > maxLen;
    let shortText = isTruncated ? text.slice(0, maxLen) + "..." : text;


    if (who === "bot") {
        msg.innerHTML = `<img class="avatar" src="shark-bot.png" alt="Bot">
        <div class="text">${shortText}
            ${isTruncated ? `<a href="#" class="see-more" title="Expandir mensaje completo">Ver más</a>` : ""}
        </div>`;
    } else {
        msg.innerHTML = `<div class="text">${shortText}
        ${isTruncated ? `<a href="#" class="see-more" title="Expandir mensaje completo">Ver más</a>` : ""}
    </div>`;
    }

    container.appendChild(msg);
    container.scrollTop = container.scrollHeight; // Solo al agregar mensaje nuevo


    // Evento para "Ver más"
    if (isTruncated) {
        const textContainer = msg.querySelector('.text');

        function toggleExpand(e) {
            e.preventDefault();
            const isExpanded = e.target.dataset.expanded === "true";

            if (isExpanded) {
                textContainer.innerHTML = `${shortText} <a href="#" class="see-more" data-expanded="false" title="Expandir mensaje completo">Ver más</a>`;
            } else {
                textContainer.innerHTML = `${text} <a href="#" class="see-more" data-expanded="true" title="Colapsar mensaje">Ver menos</a>`;
            }

            // Forzar scroll: mantener posición si estaba abajo o permitir ver hacia arriba
            const wasAtBottom = Math.abs(container.scrollHeight - container.scrollTop - container.clientHeight) < 5;
            setTimeout(() => {
                if (wasAtBottom) {
                    container.scrollTop = container.scrollHeight;
                } else {
                    container.scrollTop = container.scrollTop; // fuerza repaint para activar scroll
                }
            }, 0);

            const newLink = textContainer.querySelector('.see-more');
            newLink.addEventListener('click', toggleExpand);
        }

        const seeMoreLink = textContainer.querySelector('.see-more');
        seeMoreLink.dataset.expanded = "false";
        seeMoreLink.addEventListener('click', toggleExpand);
    }



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
function toggleLog() {
    const logPanel = document.getElementById("log-panel");
    logPanel.classList.toggle("collapsed");
}

// --- Inicialización ---
function setupWebSocketLogs() {
    const socket = new SockJS(`${BACKEND_URL}/ws`);
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
        log("🧩 Conectado al WebSocket de logs");
        stompClient.subscribe("/topic/logs", (message) => {
            log("🖧 " + message.body);
        });
    }, (error) => {
        log("❌ Error de conexión WebSocket: " + error);
    });
}


function loadChatHistory() {
    fetch(`${BACKEND_URL}/api/chat/historia`)
        .then(res => {
            if (!res.ok) throw new Error("No se pudo obtener el historial");
            return res.json();
        })
        .then(history => {
            const sidebar = document.getElementById("sidebar-history");
            let html = "<h4>Historial</h4><ul>";
            if (Array.isArray(history) && history.length > 0) {
                history.forEach(item => {
                    html += `<li>${item}</li>`;
                });
            } else {
                html += "<li>(Vacío)</li>";
            }
            html += "</ul>";
            sidebar.innerHTML = html;
            log("🕑 Historial cargado");
        })
        .catch(err => {
            log("❌ Error al cargar historial: " + err.message);
        });
}

function loadChatMemory() {
    fetch(`${BACKEND_URL}/api/chat/memoria`)
        .then(res => {
            if (!res.ok) throw new Error("No se pudo obtener la memoria");
            return res.json();
        })
        .then(memory => {
            const sidebar = document.getElementById("sidebar-memory");
            let html = "<h4>Memoria</h4>";
            if (memory && typeof memory === "string") {
                html += `<p>${memory}</p>`;
            } else if (Array.isArray(memory)) {
                html += "<ul>";
                memory.forEach(item => {
                    html += `<li>${item}</li>`;
                });
                html += "</ul>";
            } else {
                html += "<p>(Vacía)</p>";
            }
            sidebar.innerHTML = html;
            log("🧠 Memoria cargada");
        })
        .catch(err => {
            log("❌ Error al cargar memoria: " + err.message);
        });
}

document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("input-area").addEventListener("submit", handleFormSubmit);
    setupFileUpload();
    loadUploadedFiles();
    setupWebSocketLogs();
    // Mostrar mensaje del bot si no hay mensajes
    const messages = document.getElementById("messages");
    if (messages.children.length === 0) {
        const welcomeText = "¿En qué te puedo ayudar pequeño Sharkcamonte?";
        addMessage(welcomeText, "bot");

        // Esperar que el DOM del mensaje se agregue, luego scrollear a ese nodo
        setTimeout(() => {
            const allMessages = messages.querySelectorAll(".message");
            if (allMessages.length > 0) {
                allMessages[allMessages.length - 1].scrollIntoView({ behavior: "auto", block: "end" });
            }
        }, 10);
    }
    loadChatHistory();
    loadChatMemory();


});

