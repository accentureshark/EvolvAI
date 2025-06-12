import '../styles/app.css'
import { Header } from '../components/layout/Header'
import { MessagePanel } from '../components/chat/MessagePanel'
import { MemoryPanel } from '../components/memory/MemoryPanel'
import { DocumentPanel } from '../components/documents/DocumentPanel'
import { LogDisplay } from '../components/log/LogDisplay'
import { DocumentProvider } from '../contexts/DocumentContext'
import { LogProvider } from '../contexts/LogContext'
import { PromptProvider } from '../contexts/PromptContext'
import { MemoryProvider } from '../contexts/MemoryContext'
import { useWebSocketLogs } from '../hooks/useWebSocketLogs'

export const MainPage = () => {
  return (
    <LogProvider>
      <DocumentProvider>
        <PromptProvider>
          <MemoryProvider>
          <WebSocketLogger />
            <div className="main-page">
              <Header />
              <div className="main-page-container">
                <DocumentPanel />
                <MessagePanel />
                <MemoryPanel />
              </div>
              <LogDisplay />
            </ div>
          </MemoryProvider>
        </PromptProvider>
      </DocumentProvider>
    </LogProvider>
  )
}

const WebSocketLogger = () => {
  useWebSocketLogs();
  return null;
};