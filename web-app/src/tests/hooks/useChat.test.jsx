import { renderHook, act } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { useChat } from '../../hooks/useChat';
import { LogContext } from '../../contexts/LogContext';
import { DocumentContext } from '../../contexts/DocumentContext';

vi.mock('uuid', () => ({ v4: () => 'mock-uuid' }));

const mockLog = vi.fn();
const mockSelectedDocument = 'document-id';

const wrapper = ({ children }) => (
  <LogContext.Provider value={{ log: mockLog }}>
    <DocumentContext.Provider value={{ selectedDocument: mockSelectedDocument }}>
      {children}
    </DocumentContext.Provider>
  </LogContext.Provider>
);

describe('useChat', () => {
  beforeEach(() => {
    global.fetch = vi.fn(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ answer: 'Respuesta del backend' }),
        text: () => Promise.resolve('Error simulado'),
        body: {
          getReader: () => ({
            read: vi.fn()
              .mockResolvedValueOnce({
                done: false,
                value: new TextEncoder().encode('data: Primera parte\n'),
              })
              .mockResolvedValueOnce({
                done: true,
                value: new TextEncoder().encode(''),
              }),
          }),
        },
      })
    );

    vi.spyOn(window, 'alert').mockImplementation(() => {});
  });

  afterEach(() => {
    vi.resetAllMocks();
  });

  it('inicializa con mensaje de bienvenida cuando no ha comenzado el chat', () => {
    const { result } = renderHook(() => useChat(), { wrapper });
    expect(result.current.messages[0].text).toContain('Haz clic en 🆕');
  });

  it('comienza una nueva conversación correctamente', () => {
    const { result } = renderHook(() => useChat(), { wrapper });
    act(() => {
      result.current.startNewChat();
    });
    expect(result.current.messages[0].text).toContain('¿En qué te puedo ayudar');
    expect(result.current.chatStarted).toBe(true);
  });

  it('agrega mensaje del usuario y del bot en modo no streaming', async () => {
    const { result } = renderHook(() => useChat(), { wrapper });

    act(() => {
      result.current.startNewChat();
    });

    await act(async () => {
      await result.current.handleSendMessage({
        text: 'Hola',
        customPrompt: 'Prompt de prueba',
      });
    });

    const texts = result.current.messages.map(m => m.text);
    expect(texts).toContain('Hola');
    expect(texts).toContain('Respuesta del backend');
  });

  it('no envía mensaje si no hay documento seleccionado', async () => {
    const { result } = renderHook(() => useChat(), {
      wrapper: ({ children }) => (
        <LogContext.Provider value={{ log: mockLog }}>
          <DocumentContext.Provider value={{ selectedDocument: null }}>
            {children}
          </DocumentContext.Provider>
        </LogContext.Provider>
      ),
    });

    act(() => {
      result.current.startNewChat();
    });

    await act(async () => {
      await result.current.handleSendMessage({ text: 'Hola sin doc' });
    });

    expect(window.alert).toHaveBeenCalledWith('Debes seleccionar un documento primero');
  });
});
