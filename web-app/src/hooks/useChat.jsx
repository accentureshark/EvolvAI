import { useEffect, useRef, useState } from "react";
import { v4 as uuidv4 } from 'uuid';
import { useLog } from "../contexts/LogContext";
import { useDocument } from "../contexts/DocumentContext";

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

export const useChat = () => {
    const {log} = useLog();
    const {selectedDocument} = useDocument();

    const [messages, setMessages] = useState([]);
    const [conversationId, setConversationId] = useState(null);
    const [chatStarted, setChatStarted] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [useStreaming, setUseStreaming] = useState(false);

    const messageListRef = useRef(null);

    useEffect(() => {
        const list = messageListRef.current;
        if (list) {
        list.scrollTop = list.scrollHeight;
        }
    }, [messages]);

    useEffect(() => {
        if (!chatStarted) {
            setMessages([{ id: uuidv4(), text: "Haz clic en 🆕 para comenzar una conversación.", type: 'bot' }]);
        }
    }, [chatStarted]);

    const startNewChat = () => {
        setConversationId(uuidv4());
        setChatStarted(true);
        setMessages([{ id: uuidv4(), text: "¿En qué te puedo ayudar pequeño Sharkcamonte?", type: 'bot' }]);
    };

    const addMessage = (msg) => {
        setMessages(prev => [...prev, { id: Date.now() + Math.random(), ...msg }]);
    };

    const updateLastBotMessage = (text, finalize = false) => {
        setMessages(prev => {
            const updated = [...prev];
            const lastBotIndex = updated.map((m, i) => m.type === 'bot' ? i : -1).filter(i => i !== -1).pop();
            if (lastBotIndex !== undefined) {
                updated[lastBotIndex] = { ...updated[lastBotIndex], text, isComplete: finalize };
            }
            return updated;
        });
    };

    const handleSendMessage = async ({ text, customPrompt = '' }) => {
        if (!chatStarted) {
            alert("Debes iniciar una conversación primero");
            return;
        }

        // if (!selectedDocument) {
        //     alert("Debes seleccionar un documento primero");
        //     return;
        // }

        addMessage({ text, type: 'user' });

        const payload = 
        { 
            query: text,
            // documentId: selectedDocument.id,
            conversationId,
            customPrompt 
        };
        const url = `${BACKEND_URL}/api/inference/${useStreaming ? "query-stream" : "query"}`;

        log(`➡️ Enviando consulta a: ${url} | documentId: selectedDocument.id}`, "info");
        setIsLoading(true);

        try {
            useStreaming
                ? await handleStreamingResponse(url, payload)
                : await handleRegularResponse(url, payload);
        } catch (err) {
            log(`Error: ${err.message}`, "error");
            addMessage({ text: `Error de conexión: ${err.message}`, type: 'bot' });
        } finally {
            setIsLoading(false);
        }
    };

    const handleRegularResponse = async (url, payload) => {
        const res = await fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const msg = await res.text();
            log(`Error del backend: ${msg}`, "error");
            addMessage({ text: `Error del backend: ${msg}`, type: 'bot' });
            return;
        }

        const result = await res.json();
        addMessage({ text: result.answer, type: 'bot' });
    };

    const handleStreamingResponse = async (url, payload) => {
        addMessage({ text: "", type: 'bot' });

        const res = await fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const msg = await res.text();
            updateLastBotMessage(`Error del backend: ${msg}`, true);
            return;
        }

        const reader = res.body.getReader();
        const decoder = new TextDecoder();
        let buffer = "";
        let partial = "";

        while (true) {
            const { value, done } = await reader.read();
            if (done) break;

            buffer += decoder.decode(value, { stream: true });
            const lines = buffer.split('\n');
            buffer = lines.pop() || '';

            for (let line of lines) {
                line = line.replace(/^data:\s*/, '').trim();
                if (line) {
                partial += partial.endsWith(" ") || /^[.,;:?!]/.test(line) ? line : ` ${line}`;
                }
                updateLastBotMessage(partial);
            }
        }

        updateLastBotMessage(partial.replace(/\\n/g, "\n"), true);
    };

    return {
        messages,
        isLoading,
        useStreaming,
        setUseStreaming,
        startNewChat,
        handleSendMessage,
        messageListRef,
        chatStarted,
    };
}