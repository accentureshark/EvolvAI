import { useContext, useCallback } from 'react';
import { AppContext } from '../AppContext';
import { BACKEND_URL } from '../api/api';

function useChatEngine() {
  const {
    addMessage,
    showToast,
    setModalOutput,
    setModalLinks,
    setModalVisible,
  } = useContext(AppContext);

  const sendMessage = useCallback(async (query, customPrompt = '') => {
    if (!query.trim()) return;
    addMessage(query, 'user');

    try {
      const res = await fetch(`${BACKEND_URL}/api/inference/query`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query, customPrompt })
      });
      if (!res.ok) throw new Error('Error en la respuesta del bot');
      const data = await res.json();
      addMessage(data.answer || 'Sin respuesta', 'bot');
    } catch (err) {
      addMessage('❌ Error: ' + err.message, 'bot');
    }
  }, [addMessage]);

  const loadMemory = useCallback(async () => {
    try {
      const res = await fetch(`${BACKEND_URL}/chat-memory`);
      if (!res.ok) throw new Error('No se pudo obtener la memoria');
      const memory = await res.json();
      return memory;
    } catch (err) {
      showToast('❌ Error al cargar memoria: ' + err.message, true);
      return null;
    }
  }, [showToast]);

  const loadPrompt = useCallback(async () => {
    try {
      const res = await fetch(`${BACKEND_URL}/prompt`);
      if (!res.ok) throw new Error('No se pudo obtener el prompt');
      return await res.text();
    } catch (err) {
      showToast('❌ Error al cargar prompt: ' + err.message, true);
      return '';
    }
  }, [showToast]);

  const loadActuator = useCallback(async (path = '/actuator') => {
    try {
      const res = await fetch(`${BACKEND_URL}${path}`);
      if (!res.ok) throw new Error('No se pudo obtener ' + path);
      const json = await res.json();
      setModalOutput(JSON.stringify(json, null, 2));
      const links = Object.entries(json._links || {}).map(([k, v]) => [k, v.href.replace(BACKEND_URL, '')]);
      setModalLinks(links);
      setModalVisible(true);
    } catch (err) {
      setModalOutput(`Error al obtener ${path}: ${err.message}`);
      setModalVisible(true);
    }
  }, [setModalOutput, setModalLinks, setModalVisible]);

  return {
    sendMessage,
    loadMemory,
    loadPrompt,
    loadActuator,
  };
}

export default useChatEngine;