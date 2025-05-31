// utils.js
// Funciones auxiliares generales para la app modularizada

export function log(msg) {
    const logEntries = document.getElementById("log-entries");
    if (!logEntries) return;
    logEntries.innerHTML += "\n" + msg;
    logEntries.scrollTop = logEntries.scrollHeight;
}

export function showSpinner(show) {
    let spinner = document.getElementById("spinner");
    if (!spinner) {
        spinner = document.createElement("div");
        spinner.id = "spinner";
        spinner.className = "spinner";
        spinner.innerHTML = `<img src="logo.png" alt="Cargando..." class="spinner-logo">`;
        document.getElementById("chat-area")?.prepend(spinner);
    }
    spinner.style.display = show ? "flex" : "none";
}

