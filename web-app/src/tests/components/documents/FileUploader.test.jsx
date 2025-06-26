import { render, screen, fireEvent } from '@testing-library/react';
import { FileUploader } from '../../../components/documents/FileUploader';
import { vi } from 'vitest';

const fetchMock = vi.fn();
const logMock = vi.fn();

vi.mock('../../../contexts/DocumentContext', () => ({
  useDocument: () => ({
    fetchDocuments: fetchMock,
  }),
}));

vi.mock('../../../contexts/LogContext', () => ({
  useLog: () => ({
    log: logMock,
  }),
}));

vi.mock('../../../contexts/ToastContext', () => ({
  useToast: () => ({
    current: {
      show: vi.fn(),
    },
  }),
}));

vi.mock('primereact/fileupload', () => ({
  FileUpload: (props) => (
    <div>
      <button onClick={() => props.uploadHandler({ files: [new File(['dummy'], 'test.txt')] })}>
        Simular subida
      </button>
      {props.emptyTemplate}
    </div>
  ),
}));

describe('FileUploader', () => {
  beforeEach(() => {
    fetchMock.mockClear();
    logMock.mockClear();
  });

  it('renderiza el componente con la plantilla vacía', () => {
    render(<FileUploader />);
    expect(screen.getByText('Sube acá tus documentos')).toBeInTheDocument();
  });

  it('llama a log y fetchDocuments al subir un archivo exitosamente', async () => {
    global.fetch = vi.fn()
      .mockResolvedValueOnce({ ok: true })
      .mockResolvedValueOnce({ ok: true, json: vi.fn().mockResolvedValue([]) });

    render(<FileUploader />);
    fireEvent.click(screen.getByText('Simular subida'));

    await new Promise(r => setTimeout(r, 0));

    expect(logMock).toHaveBeenCalledWith(expect.stringContaining('Subiendo archivo'), 'info');
    expect(fetchMock).toHaveBeenCalled();
  });

  it('muestra error si falla la subida', async () => {
    global.fetch = vi.fn().mockResolvedValueOnce({
      ok: false,
      text: async () => 'Error del servidor',
    });

    render(<FileUploader />);
    fireEvent.click(screen.getByText('Simular subida'));

    await new Promise(r => setTimeout(r, 0));

    expect(logMock).toHaveBeenCalledWith(expect.stringContaining('Error al subir el documento'), 'error');
  });
});
