import {useState} from 'react';
import { usePrompt } from '../../contexts/PromptContext';
import {InputField} from '../ui/InputField';
import {Checkbox} from '../ui/Checkbox';
import {CustomButton} from '../ui/CustomButton';

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
        <CustomButton 
          label='New'
          className='p-button-sm p-button-rounded'
          onClick={() => startNewChat()} 
        />
        <InputField disabled={!chatStarted} value={message} onChange={(e) => setMessage(e.target.value)}/>
        <Checkbox onChange={(e) => setUseStreaming(e.target.checked)} checked={useStreaming} />
        <CustomButton
          disabled={!chatStarted || message.trim() === ''}
          type="submit"
          icon="pi pi-send"
          className="p-button-sm p-button-rounded p-button-outlined"
          label='Enviar'
        />
    </form>
  )
}
