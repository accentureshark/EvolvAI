import { render, waitFor, screen, act } from '@testing-library/react';
import { PromptProvider, usePrompt } from '../../contexts/PromptContext';
import { vi } from 'vitest';

vi.mock('../../contexts/LogContext', () => ({
  useLog: () => ({
    log: vi.fn(),
  }),
}));

// Mock del fetch global
global.fetch = vi.fn();

const TestComponent = () => {
  const { prompt, setPrompt } = usePrompt();

  return (
    <div>
      <div data-testid="prompt">{prompt}</div>
      <button onClick={() => setPrompt('Prompt modificado')}>Cambiar prompt</button>
    </div>
  );
};

describe('PromptContext', () => {
  beforeEach(() => {
    fetch.mockReset();
  });

  it('carga correctamente el prompt por defecto', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      text: async () => 'Prompt de prueba',
    });

    render(
      <PromptProvider>
        <TestComponent />
      </PromptProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId('prompt').textContent).toBe('Prompt de prueba');
    });
  });

  it('maneja errores si el fetch falla', async () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {});
    fetch.mockResolvedValueOnce({ ok: false });

    render(
      <PromptProvider>
        <TestComponent />
      </PromptProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId('prompt').textContent).toBe('');
    });

    consoleError.mockRestore();
  });

  it('setPrompt actualiza el valor del prompt', () => {
    render(
      <PromptProvider>
        <TestComponent />
      </PromptProvider>
    );

    act(() => {
      screen.getByText('Cambiar prompt').click();
    });

    expect(screen.getByTestId('prompt').textContent).toBe('Prompt modificado');
  });
});
