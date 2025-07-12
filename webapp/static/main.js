// main.js
import { setupFileUpload, loadUploadedFiles, selectedDocumentId } from './fileUpload.js';
import { addMessage } from './messages.js';
import { loadChatMemory } from './memory.js';
import { loadDefaultPrompt, togglePrompt } from './prompt.js';
import { setupWebSocketLogs } from './websocket.js';
import { setupModal } from './modal.js';
import { setupGitHubRepositories, getSelectedRepository } from './github.js';
import { log, showSpinner } from './utils.js';
import { BACKEND_URL } from './api.js';
import { v4 as uuidv4 } from 'https://cdn.jsdelivr.net/npm/uuid@9.0.0/+esm';

let conversationId = null;
let chatStarted = false;

document.addEventListener("DOMContentLoaded", () => {
    const promptPanel = document.getElementById("prompt-panel");
    const toggleBtn = document.getElementById("prompt-toggle");
    const header = document.getElementById("prompt-header");
    const customPrompt = document.getElementById("custom-prompt");

    const isCollapsed = localStorage.getItem("promptCollapsed") !== "false";
    promptPanel?.classList.toggle("collapsed", isCollapsed);
    toggleBtn.textContent = isCollapsed ? "▲" : "▼";
    header?.addEventListener("click", togglePrompt);

    setupFileUpload();
    loadUploadedFiles();
    setupGitHubRepositories();
    loadChatMemory();
    loadDefaultPrompt();
    setupWebSocketLogs();
    setupModal();

    [promptPanel, customPrompt].forEach(panel => {
        if (panel) {
            panel.style.cssText = "max-width:100%;width:100%;box-sizing:border-box;";
        }
    });

    // Referencias a los elementos
    const form = document.getElementById('input-area');
    const userInput = document.getElementById('user-input');
    const sendBtn = document.getElementById('send-btn');
    const messages = document.getElementById('messages');

    // Deshabilitar input y botón de enviar al inicio
    userInput.disabled = true;
    sendBtn.disabled = true;

    // Mensaje inicial
    messages.innerHTML = `<div class="message bot"><div class="text">Haz clic en <b>🆕</b> para comenzar una conversación.</div></div>`;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!chatStarted) {
            alert("Debes iniciar una nueva conversación antes de preguntar.");
            return;
        }
        const msg = userInput.value.trim();
        if (!msg) return;
        if (!selectedDocumentId) {
            alert("Seleccioná un documento antes de consultar.");
            return;
        }
        addMessage(msg, "user");
        userInput.value = "";

        const payload = {
            query: msg,
            documentId: selectedDocumentId,
            conversationId: getConversationId(),
            customPrompt: customPrompt?.value || ''
        };

        const useStreaming = document.getElementById("stream-toggle")?.checked;
        const url = `${BACKEND_URL}/api/inference/${useStreaming ? "query-stream" : "query"}`;
        log(`➡️ Enviando consulta a: ${url} | documentId: ${selectedDocumentId}`);

        try {
            if (useStreaming) {
                addMessage("", "bot");
                const response = await fetch(url, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
                });
                if (!response.ok) {
                    const errMsg = await response.text();
                    log(`❌ Error del backend: ${errMsg}`);
                    updateLastBotMessage(`Error del backend: ${errMsg}`, true);
                    return;
                }
                const reader = response.body.getReader();
                const decoder = new TextDecoder();
                let buffer = "", partial = "";
                while (true) {
                    const { value, done } = await reader.read();
                    if (done) break;
                    buffer += decoder.decode(value, { stream: true });
                    const lines = buffer.split('\n');
                    buffer = lines.pop() || '';
                    lines.forEach(line => {
                        line = line.replace(/^data:\s*/, '').trim();
                        if (line) partial += partial.endsWith(" ") || line.match(/^[.,;:?!]/) ? line : ` ${line}`;
                        updateLastBotMessage(partial);
                    });
                }
                if (buffer) partial += buffer.trim();
                updateLastBotMessage(partial.replace(/\\n/g, "\n"), true);
            } else {
                showSpinner(true);
                const response = await fetch(url, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
                });
                if (!response.ok) {
                    const errMsg = await response.text();
                    log(`❌ Error del backend: ${errMsg}`);
                    addMessage(`Error del backend: ${errMsg}`, "bot");
                    return;
                }
                const result = await response.json();
                addMessage(result.answer, "bot");
            }
        } catch (err) {
            log(`❌ Error conectando al backend: ${err.message}`);
            addMessage(`Error de conexión: ${err.message}`, "bot");
        } finally {
            showSpinner(false);
        }
    });

    // Habilitar input y botón al iniciar nueva conversación
    document.getElementById('new-chat-btn').addEventListener('click', () => {
        conversationId = uuidv4();
        chatStarted = true;
        messages.innerHTML = '';
        addMessage("¿En qué te puedo ayudar pequeño Tiburoncin?", "bot");
        userInput.disabled = false;
        sendBtn.disabled = false;
        userInput.focus();
    });
});

function updateLastBotMessage(text, finalize = false) {
    const container = document.getElementById("messages");
    const last = [...container.querySelectorAll(".message.bot")].pop();
    if (!last) {
        addMessage(text, "bot");
        return;
    }
    const textDiv = last.querySelector(".text");
    if (textDiv) {
        textDiv.textContent = text;
        if (finalize) container.scrollTop = container.scrollHeight;
    }
}

export function getConversationId() {
    return conversationId;
}

window.toggleLog = () => {
    const logPanel = document.getElementById('log-panel');
    const logToggle = document.getElementById('log-toggle');
    logPanel.classList.toggle('collapsed');
    logToggle.style.transform = logPanel.classList.contains('collapsed') ? 'rotate(-90deg)' : 'rotate(0deg)';
};