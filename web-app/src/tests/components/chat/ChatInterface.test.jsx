import { render, screen } from '@testing-library/react';
import { ChatInterface } from '../../../components/chat/ChatInterface';
import { vi } from 'vitest';

vi.mock('../../../components/chat/MessageItem', () => ({
  MessageItem: ({ text }) => <div data-testid="message">{text}</div>
}));

vi.mock('../../../components/chat/ChatInputBar', () => ({
  ChatInputBar: (props) => (
    <div data-testid="chat-input-bar">
      ChatInputBar
      <button onClick={() => props.onSendMessage({ text: 'test', type: 'user' })}>Enviar</button>
    </div>
  )
}));

vi.mock('../../../components/ui/TypingIndicator', () => ({
  TypingIndicator: () => <div data-testid="typing-indicator">Typing...</div>
}));

vi.mock('../../../hooks/useChat', () => ({
  useChat: () => ({
    messages: [
      { id: 1, text: 'Hola bot', type: 'user' },
      { id: 2, text: 'Hola humano', type: 'bot' }
    ],
    isLoading: true,
    startNewChat: vi.fn(),
    handleSendMessage: vi.fn(),
    messageListRef: { current: null },
    chatStarted: true
  })
}));

describe('ChatInterface', () => {
  it('renderiza los mensajes y el input', () => {
    render(<ChatInterface />);
    expect(screen.getAllByTestId('message')).toHaveLength(2);
    expect(screen.getByText('Hola bot')).toBeInTheDocument();
    expect(screen.getByText('Hola humano')).toBeInTheDocument();
    expect(screen.getByTestId('chat-input-bar')).toBeInTheDocument();
  });

});
