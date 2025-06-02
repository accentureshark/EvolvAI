import { useEffect } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL || "http://localhost:8081";

function useWebSocketLogs(onLog) {
  useEffect(() => {
    const socket = new SockJS(`${BACKEND_URL}/ws`);
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
      console.log("🧩 Conectado al WebSocket de logs");
      stompClient.subscribe("/topic/logs", (message) => {
        console.log("🖧", message.body);
        if (onLog) onLog(message.body);
      });
    }, (error) => {
      console.error("❌ Error de conexión WebSocket:", error);
    });

    return () => {
      if (stompClient.connected) {
        stompClient.disconnect(() => {
          console.log("🔌 Desconectado de WebSocket");
        });
      }
    };
  }, [onLog]);
}

export default useWebSocketLogs;
