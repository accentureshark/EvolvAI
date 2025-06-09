import { log } from './utils.js';

import { BACKEND_URL } from './api.js';

export function setupWebSocketLogs() {
    const socket = new SockJS(`${BACKEND_URL}/ws`);

    const stompClient = Stomp.over(socket);
    stompClient.connect({}, () => {
        log("🧩 Conectado al WebSocket de logs");
        stompClient.subscribe("/topic/logs", (message) => {
            log("🖧 " + message.body);
        });
    }, (error) => {
        log("❌ Error de conexión WebSocket: " + error);
    });
}
