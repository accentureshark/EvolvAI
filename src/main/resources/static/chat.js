const BACKEND_URL = "http://localhost:8081";

// --- Generación de ID de conversación único para la sesión ---
const SESSION_CONVERSATION_ID = crypto.randomUUID();

function getConversationId() {
    return SESSION_CONVERSATION_ID;
}

function setConversationId(id) {
    // no-op: ya no se cambia dinámicamente
}


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
document.getElementById("settings-button").addEventListener("click", () => {
    document.getElementById("actuator-modal").classList.remove("hidden");
    document.body.style.overflow = 'hidden';
    setTimeout(() => {
        const firstLink = document.querySelector("#actuator-modal ul a");
        if (firstLink) firstLink.focus();
    }, 100);
});


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
    console.log("🔍 handleFormSubmit triggered");
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
                const data = await res.json();
                addMessage(data.answer || JSON.stringify(data), 'bot');
                log('Respuesta recibida del backend.');

                // Guardar en memoria
                const memoryPayload = {
                    conversationId: conversationId,
                    messages: [
                        {type: "user", text: text},
                        {type: "ai", text: data.answer}
                    ]
                };
                fetch(`${BACKEND_URL}/chat-memory/`, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(memoryPayload)
                }).then(() => log('🧠 Memoria actualizada'))
                    .catch(err => log('⚠️ Error al guardar memoria: ' + err.message));

                // Guardar en historial si hay conversación
                const historyPayload = {
                    conversationId: conversationId,
                    query: text,
                    answer: data.answer
                };
                fetch(`${BACKEND_URL}/chat-history/`, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(historyPayload)
                }).then(() => log('📝 Historial guardado'))
                    .catch(err => log('⚠️ Error al guardar historial: ' + err.message));
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
            const conversationId = getConversationId();
            const customPrompt = document.getElementById("custom-prompt")?.value || null;

            const payload = {
                query: text,
                conversationId: conversationId,
                customPrompt: customPrompt
            };

            console.log("📤 Payload enviado:", payload);

            const res = await fetch(`${BACKEND_URL}/api/inference/query`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload)
            });

            if (res.ok) {
                const data = await res.json();
                addMessage(data.answer || JSON.stringify(data), 'bot');
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

// --- Reactivar carga de memoria e historial ---

function loadChatMemory() {
    fetch(`${BACKEND_URL}/chat-memory`)
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

function togglePrompt() {
    const promptPanel = document.getElementById("prompt-panel");
    const toggleBtn = document.getElementById("prompt-toggle");

    const collapsed = promptPanel.classList.toggle("collapsed");
    toggleBtn.textContent = collapsed ? "▲" : "▼";
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

function togglePrompt() {
    const promptPanel = document.getElementById("prompt-panel");
    const toggleBtn = document.getElementById("prompt-toggle");

    const collapsed = promptPanel.classList.toggle("collapsed");
    toggleBtn.textContent = "▲";
    localStorage.setItem("promptCollapsed", collapsed ? "true" : "false");
}

function loadDefaultPrompt() {
    fetch(`${BACKEND_URL}/api/llm/prompt`)
        .then(res => res.ok ? res.text() : Promise.reject("No se pudo obtener el prompt por defecto"))
        .then(text => {
            const textarea = document.getElementById("custom-prompt");
            if (textarea && textarea.value.trim() === "") {
                textarea.value = text;
                log("📝 Prompt por defecto cargado.");
            }
        })
        .catch(err => {
            log("❌ Error al cargar el prompt por defecto: " + err.message);
        });
}

function toggleLog() {
    const logPanel = document.getElementById("log-panel");
    const toggleBtn = document.getElementById("log-toggle");

    const collapsed = logPanel.classList.toggle("collapsed");
    toggleBtn.textContent = collapsed ? "▲" : "▼";
}

document.addEventListener("DOMContentLoaded", function () {
    const promptPanel = document.getElementById("prompt-panel");
    const toggleBtn = document.getElementById("prompt-toggle");
    const header = document.getElementById("prompt-header");
    const customPrompt = document.getElementById("custom-prompt");

    // Estado inicial colapsado si no está definido
    const isCollapsed = localStorage.getItem("promptCollapsed") !== "false";

    if (isCollapsed) {
        promptPanel?.classList.add("collapsed");
        toggleBtn.textContent = "▲";
    } else {
        promptPanel?.classList.remove("collapsed");
        toggleBtn.textContent = "▼";
    }

    header?.addEventListener("click", function () {
        togglePrompt();
    });

    setupFileUpload();
    loadUploadedFiles();
    setupWebSocketLogs();

    // Estilos responsivos
    if (promptPanel) {
        promptPanel.style.maxWidth = "100%";
        promptPanel.style.width = "100%";
        promptPanel.style.boxSizing = "border-box";
    }
    if (customPrompt) {
        customPrompt.style.width = "100%";
        customPrompt.style.maxWidth = "100%";
        customPrompt.style.boxSizing = "border-box";
    }

    const messages = document.getElementById("messages");
    if (messages.children.length === 0) {
        const welcomeText = "¿En qué te puedo ayudar pequeño Sharkcamonte?";
        addMessage(welcomeText, "bot");

        // Esperar que el DOM del mensaje se agregue, luego scrollear a ese nodo
        setTimeout(() => {
            const allMessages = messages.querySelectorAll(".message");
            if (allMessages.length > 0) {
                allMessages[allMessages.length - 1].scrollIntoView({behavior: "auto", block: "end"});
            }
        }, 10);
    }

    loadChatMemory();

    loadDefaultPrompt();
});

document.getElementById("settings-button").addEventListener("click", () => {
    document.getElementById("actuator-modal").classList.remove("hidden");
    document.body.style.overflow = 'hidden';
    setTimeout(() => {
        const firstLink = document.querySelector("#actuator-modal ul a");
        if (firstLink) firstLink.focus();
    }, 100);
});

document.getElementById("close-modal").addEventListener("click", () => {
    document.getElementById("actuator-modal").classList.add("hidden");
    document.body.style.overflow = 'auto';
});

document.getElementById("actuator-modal").addEventListener("click", (e) => {
    if (e.target.id === "actuator-modal") {
        e.currentTarget.classList.add("hidden");
        document.body.style.overflow = 'auto';
    }
});

document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
        const modal = document.getElementById("actuator-modal");
        if (!modal.classList.contains("hidden")) {
            modal.classList.add("hidden");
            document.body.style.overflow = 'auto';
        }
    }
});

function loadActuator(path) {
    fetch(`${BACKEND_URL}${path}`)
        .then(res => res.ok ? res.json() : Promise.reject(res))
        .then(json => {
            const pretty = JSON.stringify(json, null, 2);
            const output = document.getElementById("actuator-output");
            output.textContent = pretty;

            // Si estamos en /actuator, mostrar los links si están
            if (path === "/actuator" && json._links) {
                const list = document.querySelector("#actuator-modal ul");
                list.innerHTML = ""; // limpiamos la lista actual

                Object.entries(json._links).forEach(([key, value]) => {
                    if (typeof value.href === "string") {
                        const li = document.createElement("li");
                        li.innerHTML = `<a href="#" onclick="loadActuator('${value.href.replace(BACKEND_URL, '')}')">${key}</a>`;
                        list.appendChild(li);
                    }
                });
            }
        })
        .catch(err => {
            document.getElementById("actuator-output").textContent = `Error al obtener ${path}: ${err.status || err}`;
        });
}

function loadDefaultPrompt() {
    fetch(`${BACKEND_URL}/api/llm/prompt`)
        .then(res => {
            if (!res.ok) throw new Error("No se pudo obtener el prompt por defecto");
            return res.text();
        })
        .then(prompt => {
            const textarea = document.getElementById("custom-prompt");
            if (textarea && textarea.value.trim() === "") {
                textarea.value = prompt;
                log("📝 Prompt por defecto cargado.");
            }
        })
        .catch(err => {
            log("❌ Error al cargar el prompt por defecto: " + err.message);
        });
}


function copyActuatorOutput() {
    const text = document.getElementById("actuator-output").textContent;
    navigator.clipboard.writeText(text).then(() => {
        showToast("Contenido copiado al portapapeles");
    }, () => {
        showToast("Error al copiar", true);
    });
}

function showToast(message, isError = false) {
    const toast = document.getElementById("toast");
    document.getElementById("toast-msg").textContent = message;
    toast.style.background = isError ? '#dc2626' : '#4c1d95';
    toast.style.display = "block";
    setTimeout(() => {
        toast.style.display = "none";
    }, 3000);
}

document.getElementById("input-area").addEventListener("submit", handleFormSubmit);

document.getElementById("toast-close").addEventListener("click", () => {
    document.getElementById("toast").style.display = "none";
});
