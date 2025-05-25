// Lógica de prompt personalizado
import { log } from './utils.js';
import { fetchDefaultPrompt } from './api.js';

export function loadDefaultPrompt() {
    fetchDefaultPrompt()
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

export function togglePrompt() {
    const promptPanel = document.getElementById("prompt-panel");
    const toggleBtn = document.getElementById("prompt-toggle");
    const collapsed = promptPanel.classList.toggle("collapsed");
    toggleBtn.textContent = collapsed ? "▲" : "▼";
    localStorage.setItem("promptCollapsed", collapsed ? "true" : "false");
}

