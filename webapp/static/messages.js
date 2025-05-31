// messages.js
// Lógica de mensajes y truncado

import { log } from './utils.js';
import { showSpinner } from './utils.js';
import { BACKEND_URL } from './api.js';

export function addMessage(text, who) {
    const container = document.getElementById("messages");
    const msg = document.createElement("div");
    msg.className = "message " + who;

    // Lógica de truncado
    const maxLen = 200;
    let isTruncated = text.length > maxLen;
    let shortText = isTruncated ? text.slice(0, maxLen) + "..." : text;

    if (who === "bot") {
        msg.innerHTML = `
            <img class="avatar" src="shark-bot.png" alt="Bot">
            <div class="text">${shortText}
                ${isTruncated ? `<a href="#" class="see-more" title="Expandir mensaje completo">Ver más</a>` : ""}
            </div>
        `;
    } else {
        msg.innerHTML = `
            <div class="text">${shortText}
                ${isTruncated ? `<a href="#" class="see-more" title="Expandir mensaje completo">Ver más</a>` : ""}
            </div>
        `;
    }

    container.appendChild(msg);
    container.scrollTop = container.scrollHeight;

    // Evento para "Ver más"
    if (isTruncated) {
        const textContainer = msg.querySelector('.text');
        function toggleExpand(e) {
            e.preventDefault();
            const isExpanded = e.target.dataset.expanded === "true";
            if (isExpanded) {
                textContainer.innerHTML = `
                    ${shortText}
                    <a href="#" class="see-more" data-expanded="false" title="Expandir mensaje completo">Ver más</a>
                `;
            } else {
                textContainer.innerHTML = `
                    ${text}
                    <a href="#" class="see-more" data-expanded="true" title="Colapsar mensaje">Ver menos</a>
                `;
            }
            const wasAtBottom = Math.abs(container.scrollHeight - container.scrollTop - container.clientHeight) < 5;
            setTimeout(() => {
                if (wasAtBottom) {
                    container.scrollTop = container.scrollHeight;
                } else {
                    container.scrollTop = container.scrollTop;
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

// --- Lógica para enviar mensajes del usuario y recibir respuesta del bot ---
export function setupSendMessage() {
    const form = document.getElementById('input-area');
    const input = document.getElementById('user-input');
    const customPrompt = document.getElementById('custom-prompt');

    form.addEventListener('submit', async function (e) {
        e.preventDefault();
        const text = input.value.trim();
        if (!text) return;

        // Mostrar mensaje del usuario en la UI
        addMessage(text, 'user');
        input.value = '';
        showSpinner(true);

        try {
            // Armamos el body del request, e inyectamos documentId si existe
            const body = {
                query: text,
                customPrompt: customPrompt?.value || ''
            };

            if (window.currentDocumentId) {
                body.documentId = window.currentDocumentId;
            }

            const res = await fetch(`${BACKEND_URL}/api/inference/query`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });

            if (!res.ok) {
                // Leer el texto de error para mostrarlo en la UI
                const errText = await res.text();
                throw new Error(errText || 'Error en la respuesta del bot');
            }

            const data = await res.json();
            addMessage(data.answer || 'Sin respuesta', 'bot');
        } catch (err) {
            addMessage('❌ Error: ' + err.message, 'bot');
        } finally {
            showSpinner(false);
        }
    });
}
