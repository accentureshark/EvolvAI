import { render, waitFor, screen, act } from '@testing-library/react';
import { DocumentProvider, useDocument } from '../../contexts/DocumentContext';
import { vi } from 'vitest';

const mockLog = vi.fn();

vi.mock('../../contexts/LogContext', () => ({
  useLog: () => ({ log: mockLog }),
}));

global.fetch = vi.fn();

const TestComponent = () => {
  const { documents, selectedDocument, setSelectedDocument } = useDocument();

  return (
    <div>
      <div data-testid="document-count">{documents.length}</div>
      <div data-testid="selected-document">{selectedDocument ? selectedDocument.name : 'Ninguno'}</div>
      <button onClick={() => setSelectedDocument({ name: 'Manual.docx' })}>Cambiar</button>
    </div>
  );
};

describe('DocumentContext', () => {
  beforeEach(() => {
    fetch.mockReset();
    mockLog.mockReset();
  });

  it('carga documentos correctamente y selecciona el primero', async () => {
    const fakeDocs = [
      { id: 1, name: 'Doc1.pdf' },
      { id: 2, name: 'Doc2.pdf' }
    ];

    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => fakeDocs
    });

    render(
      <DocumentProvider>
        <TestComponent />
      </DocumentProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId('document-count').textContent).toBe('2');
      expect(screen.getByTestId('selected-document').textContent).toBe('Doc1.pdf');
    });
  });

  it('maneja error al cargar documentos', async () => {
    fetch.mockResolvedValueOnce({ ok: false, status: 500, statusText: 'Internal Server Error' });

    render(
      <DocumentProvider>
        <TestComponent />
      </DocumentProvider>
    );

    await waitFor(() => {
      expect(mockLog).toHaveBeenCalledWith(expect.stringContaining('Error cargando documentos'), 'error');
    });

    expect(screen.getByTestId('document-count').textContent).toBe('0');
    expect(screen.getByTestId('selected-document').textContent).toBe('Ninguno');
  });

  it('setSelectedDocument funciona correctamente', () => {
    render(
      <DocumentProvider>
        <TestComponent />
      </DocumentProvider>
    );

    act(() => {
      screen.getByText('Cambiar').click();
    });

    expect(screen.getByTestId('selected-document').textContent).toBe('Manual.docx');
  });
});
