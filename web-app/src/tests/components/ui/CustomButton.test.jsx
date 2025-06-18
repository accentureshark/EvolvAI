import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { CustomButton } from '../../../components/ui/CustomButton';

describe('CustomButton', () => {
  it('muestra el label correctamente', () => {
    render(<CustomButton label="Hola Mundo" />);
    expect(screen.getByText('Hola Mundo')).toBeInTheDocument();
  });

  it('ejecuta la función onClick al hacer clic', () => {
    const handleClick = vi.fn();
    render(<CustomButton label="Click" onClick={handleClick} />);
    fireEvent.click(screen.getByRole('button', { name: 'Click' }));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('está deshabilitado cuando `disabled=true`', () => {
    render(<CustomButton label="No Click" disabled />);
    const btn = screen.getByRole('button', { name: 'No Click' });
    expect(btn).toBeDisabled();
  });

  it('muestra el tooltip si se pasa (sin romper)', () => {
    render(<CustomButton label="Info" tooltip="Soy un tooltip" />);
    const btn = screen.getByRole('button', { name: 'Info' });
    expect(btn).toBeInTheDocument(); // comprobamos que se monta bien
  });
});
