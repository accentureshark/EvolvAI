import {useState} from 'react';
import { usePrompt } from '../../contexts/PromptContext';
import {TextareaField} from '../ui/TextareaField'
import {CustomButton} from '../ui/CustomButton';

export const ChatInputBar = (
  {
    onSendMessage,
    startNewChat,
    chatStarted,
  }
) => {
  const [message, setMessage] = useState('');
  const { prompt } = usePrompt();

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    if (message.trim() === '') return;

    const messageData = {
      text: message,
      type: 'user',
      useStreaming: true,
      customPrompt: prompt
    };

    console.log('Saving message:', messageData);
    
    onSendMessage(messageData);
    setMessage('');
  }

  return (
    <form onSubmit={handleSubmit} className='chat-form'>
        <CustomButton 
          label='New'
          className='p-button p-button-rounded'
          onClick={() => startNewChat()} 
        />
        <TextareaField
          placeholder="Escribe un mensaje..."
          name="send-message"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          onKeyDown={handleKeyDown}
          autoResize={true}
          disabled={!chatStarted}
          className="chat-textarea"
        />
        <CustomButton
          disabled={!chatStarted || message.trim() === ''}
          type="submit"
          icon="pi pi-send"
          className="p-button p-button-rounded p-button-outlined"
          label='Enviar'
        />
    </form>
  )
}
