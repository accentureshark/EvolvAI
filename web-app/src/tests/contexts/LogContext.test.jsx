import { render, screen, fireEvent } from '@testing-library/react';
import { LogProvider, useLog } from '../../contexts/LogContext';

const TestComponent = () => {
  const { logs, log } = useLog();

  return (
    <div>
      <button onClick={() => log('Nuevo log', 'info')}>Agregar log</button>
      <ul>
        {logs.map((entry, index) => (
          <li key={index}>
            [{entry.type}] {entry.message}
          </li>
        ))}
      </ul>
    </div>
  );
};

describe('LogContext', () => {
  it('muestra el log inicial por defecto', () => {
    render(
      <LogProvider>
        <TestComponent />
      </LogProvider>
    );

    expect(screen.getByText(/🖥️ Consola iniciada/i)).toBeInTheDocument();
  });

  it('agrega un nuevo log al hacer click en el botón', () => {
    render(
      <LogProvider>
        <TestComponent />
      </LogProvider>
    );

    fireEvent.click(screen.getByText('Agregar log'));

    expect(screen.getByText('[info] Nuevo log')).toBeInTheDocument();
  });

  it('muestra el log más reciente primero', () => {
    render(
      <LogProvider>
        <TestComponent />
      </LogProvider>
    );

    fireEvent.click(screen.getByText('Agregar log'));

    const logItems = screen.getAllByRole('listitem');
    expect(logItems[0].textContent).toBe('[info] Nuevo log');
  });
});
