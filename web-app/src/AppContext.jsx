import React, { createContext, useState, useCallback } from 'react';

export const AppContext = createContext();

export function AppProvider({ children }) {
  const [logs, setLogs] = useState([]);
  const [messages, setMessages] = useState([]);
  const [toast, setToast] = useState({ message: '', isError: false });
  const [modalVisible, setModalVisible] = useState(false);
  const [modalOutput, setModalOutput] = useState('');
  const [modalLinks, setModalLinks] = useState([]);

  const addMessage = useCallback((text, who) => {
    setMessages(prev => [...prev, { text, who }]);
  }, []);

  const showToast = useCallback((message, isError = false) => {
    setToast({ message, isError });
    setTimeout(() => {
      setToast({ message: '', isError: false });
    }, 3000);
  }, []);

  const contextValue = {
    logs,
    setLogs,
    messages,
    addMessage,
    toast,
    showToast,
    modalVisible,
    setModalVisible,
    modalOutput,
    setModalOutput,
    modalLinks,
    setModalLinks,
  };

  return <AppContext.Provider value={contextValue}>{children}</AppContext.Provider>;
}