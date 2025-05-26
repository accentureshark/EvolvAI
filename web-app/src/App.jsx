import React, { useEffect } from 'react';
import ChatMessages from './components/ChatMessages';
import PromptPanel from './components/PromptPanel';
import MemorySidebar from './components/MemorySidebar';
import FileUpload from './components/FileUpload';
import ModalActuator from './components/ModalActuator';
import Toast from './components/Toast';
import Spinner from './components/Spinner';
import LogPanel from './components/LogPanel';
import useWebSocketLogs from './hooks/useWebSocketLogs';
import './styles/app.css';

import { AppProvider } from './AppContext';

function AppContent() {
  useWebSocketLogs();

  useEffect(() => {
    console.log("Init logic here...");
  }, []);

  return (
    <div id="main-container">
      <PromptPanel />
      <FileUpload />
      <MemorySidebar />
      <ChatMessages />
      <Spinner />
      <LogPanel />
      <ModalActuator />
      <Toast />
    </div>
  );
}

function App() {
  return <AppProvider><AppContent /></AppProvider>;
}

export default App;