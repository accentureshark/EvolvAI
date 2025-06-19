import { render, screen, fireEvent } from '@testing-library/react';
import { vi } from 'vitest';

const setPromptMock = vi.fn();

vi.doMock('../../../contexts/PromptContext', () => ({
  usePrompt: () => ({
    prompt: '',
    setPrompt: setPromptMock,
  }),
}));

describe('PromptEditor', () => {
  it('llama a setPrompt al escribir en el textarea', async () => {
    const { PromptEditor } = await import('../../../components/chat/PromptEditor');

    render(<PromptEditor />);

    fireEvent.click(screen.getByText(/prompt personalizado/i));

    fireEvent.change(screen.getByPlaceholderText('Escribe tu prompt...'), {
      target: { value: 'Otro prompt' },
    });

    expect(setPromptMock).toHaveBeenCalledWith('Otro prompt');
  });
});
