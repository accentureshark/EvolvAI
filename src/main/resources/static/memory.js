// Lógica de memoria e historial
import { log } from './utils.js';
import { BACKEND_URL } from './api.js';

export function loadChatMemory() {
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

