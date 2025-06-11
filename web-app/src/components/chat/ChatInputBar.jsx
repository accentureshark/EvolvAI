import {InputField} from '../ui/InputField';
import {Checkbox} from '../ui/Checkbox';
import {useState} from 'react';
import { Button } from 'primereact/button';
import { usePrompt } from '../../contexts/PromptContext';

export const ChatInputBar = (
  {
    onSendMessage,
    startNewChat,
    useStreaming,
    setUseStreaming,
    chatStarted,
  }
) => {
  const [message, setMessage] = useState('');
  const { prompt } = usePrompt();

  const handleSubmit = (event) => {
    event.preventDefault();

    if (message.trim() === '') return;

    const messageData = {
      text: message,
      type: 'user',
      useStreaming: useStreaming,
      customPrompt: prompt
    };

    console.log('Saving message:', messageData);
    
    onSendMessage(messageData);
    setMessage('');
    setUseStreaming(false); 
  }

  return (
    <form onSubmit={handleSubmit} className='chat-form'>
        <Button 
          label='New'
          className='p-button-sm p-button-rounded'
          onClick={() => startNewChat()} 
        />
        <InputField disabled={!chatStarted} value={message} onChange={(e) => setMessage(e.target.value)}/>
        <Checkbox onChange={(e) => setUseStreaming(e.target.checked)} checked={useStreaming} />
        <Button
          disabled={!chatStarted || message.trim() === ''}
          type="submit"
          icon="pi pi-send"
          className="p-button-sm p-button-rounded p-button-outlined"
          label='Enviar'
        />
    </form>
  )
}
