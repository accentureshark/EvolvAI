import '../../styles/message.css'

import { ChatInputBar } from './ChatInputBar'
import { MessageItem } from './MessageItem';
import { TypingIndicator } from '../ui/TypingIndicator';
import { useChat } from '../../hooks/useChat';

export const ChatInterface = () => {

    const {
    messages,
    isLoading,
    startNewChat,
    handleSendMessage,
    messageListRef,
    chatStarted
  } = useChat();

  return (
    <div className='chat-interface-container'>
      <ul className='message-list' ref={messageListRef}>
        {messages.map(msg => (          
          <MessageItem
            key={msg.id}
            text={msg.text}
            type={msg.type}
          />
        ))}
        {isLoading && <TypingIndicator />}
      </ul>
      <div className='message-input'>
        <ChatInputBar 
          onSendMessage={handleSendMessage}
          startNewChat={startNewChat}
          chatStarted={chatStarted}
        />
      </div>
    </div>
  )
}
