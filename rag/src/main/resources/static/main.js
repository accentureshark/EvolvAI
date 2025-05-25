// Inicialización y eventos DOM principales
import { setupFileUpload, loadUploadedFiles } from './fileUpload.js';
import { addMessage } from './messages.js';
import { loadChatMemory } from './memory.js';
import { loadDefaultPrompt, togglePrompt } from './prompt.js';
import { setupWebSocketLogs } from './websocket.js';
import { setupModal } from './modal.js';
import { setupSendMessage } from './messages.js';

// Aquí iría la lógica de inicialización principal

document.addEventListener("DOMContentLoaded", function () {
    // Inicialización de panel de prompt
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

    // Inicialización de carga de archivos y memoria
    setupFileUpload();
    loadUploadedFiles();
    loadChatMemory();
    loadDefaultPrompt();


    // Inicialización de WebSocket y modal
    setupWebSocketLogs();
    setupModal();

    // Inicialización de envío de mensajes
    setupSendMessage();

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

    // Mensaje de bienvenida si no hay mensajes
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

    // Cargar prompt por defecto
    loadDefaultPrompt();
});

// --- Lógica para colapsar el panel de log ---
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
