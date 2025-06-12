import { useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { CompatClient, Stomp } from '@stomp/stompjs';
import { useLog } from '../contexts/LogContext';

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

export const useWebSocketLogs = () => {
  const { log } = useLog();
  const stompClientRef = useRef(/** @type {CompatClient | null} */(null));

  useEffect(() => {
    const socket = new SockJS(`${BACKEND_URL}/ws`);
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
      log("🧩 Conectado al WebSocket de logs", "info");
      stompClient.subscribe("/topic/logs", (message) => {
        if (message?.body) {
          log(`🖧 ${message.body}`, "info");
        }
      });
    }, (error) => {
      log(`❌ Error de conexión WebSocket: ${error}`, "error");
    });

    stompClientRef.current = stompClient;

    // Limpieza al desmontar
    return () => {
      if (stompClientRef.current?.connected) {
        stompClientRef.current.disconnect(() => {
          log("🔌 WebSocket de logs desconectado", "info");
        });
      }
    };
  }, []);
};
