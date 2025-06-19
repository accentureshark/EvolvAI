import { render, screen, fireEvent } from '@testing-library/react';
import { DocumentList } from '../../../components/documents/DocumentList';
import { vi } from 'vitest';

vi.mock('../../../contexts/DocumentContext', () => ({
  useDocument: vi.fn(),
}));

vi.mock('../../../components/documents/FileUploader', () => ({
  FileUploader: () => <div data-testid="mock-uploader">Uploader</div>,
}));

import { useDocument } from '../../../contexts/DocumentContext';

describe('DocumentList', () => {
  it('muestra la lista de documentos si hay documentos', () => {
    const mockSetSelected = vi.fn();

    useDocument.mockReturnValue({
      documents: ['doc1.pdf', 'doc2.pdf'],
      selectedDocument: 'doc1.pdf',
      setSelectedDocument: mockSetSelected,
    });

    render(<DocumentList />);

    expect(screen.getByText('Documentos cargados')).toBeInTheDocument();
    expect(screen.getByDisplayValue('doc1.pdf')).toBeInTheDocument();
    expect(screen.getByText('doc2.pdf')).toBeInTheDocument();
    expect(screen.getByTestId('mock-uploader')).toBeInTheDocument();
  });

  it('llama a setSelectedDocument al cambiar el documento seleccionado', () => {
    const mockSetSelected = vi.fn();

    useDocument.mockReturnValue({
      documents: ['doc1.pdf', 'doc2.pdf'],
      selectedDocument: 'doc1.pdf',
      setSelectedDocument: mockSetSelected,
    });

    render(<DocumentList />);

    fireEvent.change(screen.getByRole('combobox'), {
      target: { value: 'doc2.pdf' },
    });

    expect(mockSetSelected).toHaveBeenCalledWith('doc2.pdf');
  });

  it('no muestra el select si no hay documentos', () => {
    useDocument.mockReturnValue({
      documents: [],
      selectedDocument: null,
      setSelectedDocument: vi.fn(),
    });

    render(<DocumentList />);

    expect(screen.queryByRole('combobox')).not.toBeInTheDocument();
    expect(screen.getByTestId('mock-uploader')).toBeInTheDocument();
  });
});
