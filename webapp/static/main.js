// main.js
// Inicialización y eventos DOM principales

import { setupFileUpload, loadUploadedFiles, selectedDocumentId } from './fileUpload.js';
import { addMessage } from './messages.js';
import { loadChatMemory } from './memory.js';
import { loadDefaultPrompt, togglePrompt } from './prompt.js';
import { setupWebSocketLogs } from './websocket.js';
import { setupModal } from './modal.js';
import { log } from './utils.js';
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
            // Puedes agregar más campos aquí según tu backend (prompt, context, etc.)
        };

        log(`➡️ Enviando consulta al backend con documentId: ${selectedDocumentId}`);

        // Enviar al backend
        try {
            const response = await fetch(`${BACKEND_URL}/api/inference/query`, {
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
        } catch (err) {
            log("❌ Error conectando al backend: " + err.message);
            addMessage("Error de conexión: " + err.message, "bot");
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
