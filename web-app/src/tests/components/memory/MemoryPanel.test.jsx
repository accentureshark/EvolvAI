import { render, screen } from '@testing-library/react';
import { MemoryPanel } from '../../../components/memory/MemoryPanel';
import { vi } from 'vitest';

vi.mock('../../../contexts/MemoryContext', () => ({
  useMemory: vi.fn(),
}));

import { useMemory } from '../../../contexts/MemoryContext';

describe('MemoryPanel', () => {
  it('muestra texto de carga cuando loading es true', () => {
    useMemory.mockReturnValue({
      loading: true,
      memory: [],
    });

    render(<MemoryPanel />);

    expect(screen.getByText(/cargando memoria/i)).toBeInTheDocument();
  });

  it('muestra la lista de mensajes cuando loading es false y hay memoria', () => {
    useMemory.mockReturnValue({
      loading: false,
      memory: [
        { role: 'user', content: 'Hola' },
        { role: 'bot', content: 'Hola, ¿en qué puedo ayudarte?' },
      ],
    });

    render(<MemoryPanel />);

    expect(screen.getByText(/memoria/i)).toBeInTheDocument();

    expect(screen.getByText(/user:/i)).toBeInTheDocument();
    expect(screen.getByText(/bot:/i)).toBeInTheDocument();
    expect(screen.getByText(/Hola, ¿en qué puedo ayudarte\?/)).toBeInTheDocument();
  });

  it('muestra "(Vacía)" cuando loading es false y no hay memoria', () => {
    useMemory.mockReturnValue({
      loading: false,
      memory: [],
    });

    render(<MemoryPanel />);

    expect(screen.getByText(/\(vacía\)/i)).toBeInTheDocument();
  });
});
