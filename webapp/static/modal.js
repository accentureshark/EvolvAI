// Lógica del modal actuator y toast
import { log } from './utils.js';
import { BACKEND_URL } from './api.js';

export function setupModal() {
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
    document.getElementById("toast-close").addEventListener("click", () => {
        document.getElementById("toast").style.display = "none";
    });
}

export function showToast(message, isError = false) {
    const toast = document.getElementById("toast");
    document.getElementById("toast-msg").textContent = message;
    toast.style.background = isError ? '#dc2626' : '#4c1d95';
    toast.style.display = "block";
    setTimeout(() => {
        toast.style.display = "none";
    }, 3000);
}

export function copyActuatorOutput() {
    const text = document.getElementById("actuator-output").textContent;
    navigator.clipboard.writeText(text).then(() => {
        showToast("Contenido copiado al portapapeles");
    }, () => {
        showToast("Error al copiar", true);
    });
}

export function loadActuator(path) {
    fetch(`${BACKEND_URL}${path}`)
        .then(res => res.ok ? res.json() : Promise.reject(res))
        .then(json => {
            const pretty = JSON.stringify(json, null, 2);
            const output = document.getElementById("actuator-output");
            output.textContent = pretty;
            if (path === "/actuator" && json._links) {
                const list = document.querySelector("#actuator-modal ul");
                list.innerHTML = "";
                Object.entries(json._links).forEach(([key, value]) => {
                    if (typeof value.href === "string") {
                        const li = document.createElement("li");
                        li.innerHTML = `<a href=\"#\" onclick=\"loadActuator('${value.href.replace(BACKEND_URL, '')}')\">${key}</a>`;
                        list.appendChild(li);
                    }
                });
            }
        })
        .catch(err => {
            document.getElementById("actuator-output").textContent = `Error al obtener ${path}: ${err.status || err}`;
        });
}

