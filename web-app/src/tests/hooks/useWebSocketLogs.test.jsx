import { renderHook } from '@testing-library/react';
import { LogContext } from '../../contexts/LogContext';
import { useWebSocketLogs } from '../../hooks/useWebSocketLogs';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

vi.mock('sockjs-client', () => ({
  default: vi.fn(),
}));

const mockSubscribe = vi.fn();
const mockDisconnect = vi.fn();
const mockConnect = vi.fn((_, onConnect) => onConnect());
const mockStompClient = {
  connect: mockConnect,
  subscribe: mockSubscribe,
  disconnect: mockDisconnect,
  connected: true,
};

vi.mock('@stomp/stompjs', () => ({
  Stomp: {
    over: vi.fn(() => mockStompClient),
  },
}));

describe('useWebSocketLogs', () => {
  const mockLog = vi.fn();

  const wrapper = ({ children }) => (
    <LogContext.Provider value={{ log: mockLog }}>
      {children}
    </LogContext.Provider>
  );

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('conecta al WebSocket y suscribe al tópico /topic/logs', () => {
    renderHook(() => useWebSocketLogs(), { wrapper });

    expect(SockJS).toHaveBeenCalledWith(`${import.meta.env.VITE_BACKEND_URL}/ws`);
    expect(Stomp.over).toHaveBeenCalled();
    expect(mockConnect).toHaveBeenCalled();
    expect(mockSubscribe).toHaveBeenCalledWith("/topic/logs", expect.any(Function));
    expect(mockLog).toHaveBeenCalledWith("🧩 Conectado al WebSocket de logs", "info");
  });

  it('recibe y loguea mensajes del WebSocket', () => {
    let onMessage;
    mockSubscribe.mockImplementation((_topic, callback) => {
      onMessage = callback;
    });

    renderHook(() => useWebSocketLogs(), { wrapper });

    const fakeMessage = { body: "Mensaje de prueba" };
    onMessage(fakeMessage);

    expect(mockLog).toHaveBeenCalledWith("🖧 Mensaje de prueba", "info");
  });

  it('desconecta el WebSocket al desmontar', () => {
    const { unmount } = renderHook(() => useWebSocketLogs(), { wrapper });
    unmount();

    expect(mockDisconnect).toHaveBeenCalled();
  });
});
