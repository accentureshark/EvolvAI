// utils.js
// Funciones auxiliares generales para la app modularizada

export function log(msg) {
    const logEntries = document.getElementById("log-entries");
    if (!logEntries) return;

    // Obtener las líneas actuales (separar por saltos de línea)
    let lines = logEntries.textContent.split('\n').filter(line => line.trim() !== '');

    // Insertar el nuevo mensaje al inicio
    lines.unshift(msg);

    // Limitar a 1000 líneas
    if (lines.length > 1000) {
        lines = lines.slice(0, 1000);
    }

    // Actualizar el contenido con el nuevo orden
    logEntries.textContent = lines.join('\n');

    // Opcional: mantener el scroll arriba para que el mensaje nuevo se vea
    logEntries.scrollTop = 0;
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

