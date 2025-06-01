// main.js
import { setupFileUpload, loadUploadedFiles, selectedDocumentId } from './fileUpload.js';
import { addMessage } from './messages.js';
import { loadChatMemory } from './memory.js';
import { loadDefaultPrompt, togglePrompt } from './prompt.js';
import { setupWebSocketLogs } from './websocket.js';
import { setupModal } from './modal.js';
import { log, showSpinner } from './utils.js';
import { BACKEND_URL } from './api.js';

document.addEventListener("DOMContentLoaded", function () {
    // Panel de prompt
    const promptPanel = document.getElementById("prompt-panel");
    const toggleBtn = document.getElementById("prompt-toggle");
    const header = document.getElementById("prompt-header");
    const customPrompt = document.getElementById("custom-prompt");

    // Estado inicial del prompt (colapsado o no)
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

    // Inicializar upload y lista de archivos
    setupFileUpload();
    loadUploadedFiles();

    // Inicializar memoria de chat y prompt por defecto
    loadChatMemory();
    loadDefaultPrompt();

    // Inicializar WebSocket de logs y modal
    setupWebSocketLogs();
    setupModal();

    // Ajustes de estilos para el panel de prompt
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

    // Inicializar envío de mensajes con documentId
    const form = document.getElementById('input-area');
    const userInput = document.getElementById('user-input');

    form.addEventListener('submit', async function (e) {
        e.preventDefault();
        const msg = userInput.value.trim();
        if (!msg) return;
        if (!selectedDocumentId) {
            alert("Seleccioná un documento antes de consultar.");
            return;
        }

        // Mostramos mensaje del usuario
        addMessage(msg, "user");
        userInput.value = "";

        // Armar el payload incluyendo documentId
        const payload = {
            query: msg,
            documentId: selectedDocumentId
        };

        // Chequeá si el checkbox está activo
        const useStreaming = document.getElementById("stream-toggle")?.checked;
        const url = useStreaming
            ? `${BACKEND_URL}/api/inference/query-stream`
            : `${BACKEND_URL}/api/inference/query`;

        log(`➡️ Enviando consulta a: ${url} | documentId: ${selectedDocumentId}`);

        try {
            if (useStreaming) {
                // NO mostrar spinner en modo streaming
                addMessage("", "bot");
                const response = await fetch(url, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
                });

                if (!response.ok) {
                    const errMsg = await response.text();
                    log("❌ Error del backend: " + errMsg);
                    updateLastBotMessage("Error del backend: " + errMsg, true);
                    return;
                }

                const reader = response.body.getReader();
                let partial = "";
                let done = false;
                let decoder = new TextDecoder();
                let buffer = "";

                while (!done) {
                    const { value, done: chunkDone } = await reader.read();
                    done = chunkDone;
                    if (value) {
                        buffer += decoder.decode(value, { stream: !done });
                        let lines = buffer.split('\n');
                        buffer = lines.pop() || '';
                        for (let line of lines) {
                            line = line.replace(/^data:\s*/, '');
                            if (!line) continue;
                            if (line === "\\n" || line === "\n") {
                                partial += "\n";
                            } else if (line.trim() === "") {
                                partial += " ";
                            } else {
                                if (
                                    partial.length > 0 &&
                                    !partial.endsWith(" ") &&
                                    !partial.endsWith("\n") &&
                                    !line.match(/^[.,;:?!]/)
                                ) {
                                    partial += " ";
                                }
                                partial += line;
                            }
                            updateLastBotMessage(partial);
                        }
                    }
                }
                if (buffer.length > 0) {
                    if (buffer === "\\n" || buffer === "\n") {
                        partial += "\n";
                    } else if (buffer.trim() === "") {
                        partial += " ";
                    } else {
                        if (
                            partial.length > 0 &&
                            !partial.endsWith(" ") &&
                            !partial.endsWith("\n") &&
                            !buffer.match(/^[.,;:?!]/)
                        ) {
                            partial += " ";
                        }
                        partial += buffer;
                    }
                }
                partial = partial.replace(/\\n/g, "\n");
                updateLastBotMessage(partial, true);

            } else {
                // SOLO en REST: spinner ON
                showSpinner(true);

                const response = await fetch(url, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
                });
                if (!response.ok) {
                    const errMsg = await response.text();
                    log("❌ Error del backend: " + errMsg);
                    addMessage("Error del backend: " + errMsg, "bot");
                    return;
                }
                const result = await response.json();
                addMessage(result.answer, "bot");
            }
        } catch (err) {
            log("❌ Error conectando al backend: " + err.message);
            addMessage("Error de conexión: " + err.message, "bot");
        } finally {
            // Apaga el spinner siempre (no pasa nada si ya estaba oculto)
            showSpinner(false);
        }
    });

    // Mensaje de bienvenida si no hay ningún mensaje
    const messages = document.getElementById("messages");
    if (messages.children.length === 0) {
        const welcomeText = "¿En qué te puedo ayudar pequeño Sharkcamonte?";
        addMessage(welcomeText, "bot");
        setTimeout(() => {
            const allMessages = messages.querySelectorAll(".message");
            if (allMessages.length > 0) {
                allMessages[allMessages.length - 1].scrollIntoView({behavior: "auto", block: "end"});
            }
        }, 10);
    }
});

// Función para actualizar el último mensaje del bot incrementalmente
function updateLastBotMessage(text, finalize = false) {
    const container = document.getElementById("messages");
    const last = [...container.querySelectorAll(".message.bot")].pop();
    if (!last) {
        // Si no hay, agregalo de cero
        addMessage(text, "bot");
        return;
    }
    // Solo actualiza el texto, sin avatar
    const textDiv = last.querySelector(".text");
    if (textDiv) {
        textDiv.textContent = text;
        if (finalize) {
            // Scroll abajo si corresponde
            container.scrollTop = container.scrollHeight;
        }
    }
}

// Lógica para colapsar/expandir el panel de logs
window.toggleLog = function() {
    const logPanel = document.getElementById('log-panel');
    logPanel.classList.toggle('collapsed');
    const logToggle = document.getElementById('log-toggle');
    if (logPanel.classList.contains('collapsed')) {
        logToggle.style.transform = 'rotate(-90deg)';
    } else {
        logToggle.style.transform = 'rotate(0deg)';
    }
};
