import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { ToastProvider, useToast } from '../../contexts/ToastContext';
import { useRef } from 'react';

vi.mock('primereact/toast', () => ({
  Toast: () => <div data-testid="toast-component">Mocked Toast</div>,
}));

const TestComponent = () => {
  const toast = useToast();
  return <div data-testid="toast-ref">{toast ? 'Tiene ref' : 'No tiene ref'}</div>;
};

describe('ToastContext', () => {
  it('renderiza el ToastProvider y sus hijos', () => {
    render(
      <ToastProvider>
        <div data-testid="child">Contenido</div>
      </ToastProvider>
    );

    expect(screen.getByTestId('child')).toBeInTheDocument();
    expect(screen.getByTestId('toast-component')).toBeInTheDocument();
  });

  it('useToast retorna una ref válida', () => {
    render(
      <ToastProvider>
        <TestComponent />
      </ToastProvider>
    );

    expect(screen.getByTestId('toast-ref')).toHaveTextContent('Tiene ref');
  });
});
