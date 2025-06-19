import { render, screen } from '@testing-library/react';
import { MainPage } from '../../pages/MainPage';

vi.mock('../../hooks/useWebSocketLogs', () => ({
  useWebSocketLogs: vi.fn(),
}));

vi.mock('../../components/layout/Header', () => ({
  Header: () => <div data-testid="header">Header</div>,
}));
vi.mock('../../components/documents/DocumentPanel', () => ({
  DocumentPanel: () => <div data-testid="document-panel">Document Panel</div>,
}));
vi.mock('../../components/chat/MessagePanel', () => ({
  MessagePanel: () => <div data-testid="message-panel">Message Panel</div>,
}));
vi.mock('../../components/memory/MemoryPanel', () => ({
  MemoryPanel: () => <div data-testid="memory-panel">Memory Panel</div>,
}));
vi.mock('../../components/log/LogDisplay', () => ({
  LogDisplay: () => <div data-testid="log-display">Log Display</div>,
}));

describe('MainPage', () => {
  it('renderiza todos los componentes principales', () => {
    render(<MainPage />);

    expect(screen.getByTestId('header')).toBeInTheDocument();
    expect(screen.getByTestId('document-panel')).toBeInTheDocument();
    expect(screen.getByTestId('message-panel')).toBeInTheDocument();
    expect(screen.getByTestId('memory-panel')).toBeInTheDocument();
    expect(screen.getByTestId('log-display')).toBeInTheDocument();
  });
});
