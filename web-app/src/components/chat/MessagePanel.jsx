import '../../styles/message.css'

import { ChatInterface } from './ChatInterface';
import { PromptEditor } from './PromptEditor';

export const MessagePanel = () => {
  return (
    <div className="message-panel-container">
      <ChatInterface />
      <PromptEditor />
    </div>
  )
}
