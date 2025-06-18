import { render, waitFor } from '@testing-library/react';
import { MemoryProvider, useMemory } from '../../contexts/MemoryContext';
import { LogProvider } from '../../contexts/LogContext'; // usa el real
import { vi } from 'vitest';

global.fetch = vi.fn();

const TestComponent = () => {
  const { memory, loading } = useMemory();
  return (
    <div>
      <div data-testid="loading">{loading ? 'loading' : 'done'}</div>
      <ul>
        {memory.map((item, index) => (
          <li key={index}>{item.content}</li>
        ))}
      </ul>
    </div>
  );
};

describe('MemoryContext', () => {
  beforeEach(() => {
    fetch.mockClear();
  });

  it('carga la memoria correctamente', async () => {
    const mockMemory = [{ role: 'user', content: 'Hola' }];
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockMemory,
    });

    const { getByTestId, findByText } = render(
      <LogProvider>
        <MemoryProvider>
          <TestComponent />
        </MemoryProvider>
      </LogProvider>
    );

    expect(getByTestId('loading').textContent).toBe('loading');

    await waitFor(() => {
      expect(getByTestId('loading').textContent).toBe('done');
    });

    expect(await findByText('Hola')).toBeInTheDocument();
  });

  it('maneja error al cargar memoria', async () => {
    fetch.mockResolvedValueOnce({
      ok: false,
      status: 500,
    });

    const { getByTestId } = render(
      <LogProvider>
        <MemoryProvider>
          <TestComponent />
        </MemoryProvider>
      </LogProvider>
    );

    await waitFor(() => {
      expect(getByTestId('loading').textContent).toBe('done');
    });

    expect(fetch).toHaveBeenCalled();
  });
});
