import { render, screen, fireEvent } from '@testing-library/react';
import { ChatInputBar } from '../../../components/chat/ChatInputBar';
import { vi } from 'vitest';

vi.mock('../../../contexts/PromptContext', () => ({
  usePrompt: () => ({
    prompt: 'mocked prompt',
  }),
}));

describe('ChatInputBar', () => {
  const setup = (overrides = {}) => {
    const onSendMessage = vi.fn();
    const startNewChat = vi.fn();
    const setUseStreaming = vi.fn();

    const result = render(
      <ChatInputBar
        onSendMessage={onSendMessage}
        startNewChat={startNewChat}
        useStreaming={overrides.useStreaming ?? false}
        setUseStreaming={setUseStreaming}
        chatStarted={overrides.chatStarted ?? true}
      />
    );

    return { ...result, onSendMessage, startNewChat, setUseStreaming };
  };

  it('envía el mensaje correctamente al enviar el formulario', () => {
    const { onSendMessage, container } = setup({ useStreaming: true });

    const input = screen.getByPlaceholderText(/escribe un mensaje/i);
    fireEvent.change(input, { target: { value: 'Hola mundo' } });

    const form = container.querySelector('form');
    fireEvent.submit(form);

    expect(onSendMessage).toHaveBeenCalledWith({
      text: 'Hola mundo',
      type: 'user',
      useStreaming: true,
      customPrompt: 'mocked prompt',
    });
  });

  it('no envía si el mensaje está vacío', () => {
    const { onSendMessage, container } = setup();

    const form = container.querySelector('form');
    fireEvent.submit(form);

    expect(onSendMessage).not.toHaveBeenCalled();
  });

  it('el botón de enviar está deshabilitado si chatStarted es false', () => {
    setup({ chatStarted: false });

    const input = screen.getByPlaceholderText(/escribe un mensaje/i);
    expect(input).toBeDisabled();

    const sendButton = screen.getByRole('button', { name: /enviar/i });
    expect(sendButton).toBeDisabled();
  });
});
