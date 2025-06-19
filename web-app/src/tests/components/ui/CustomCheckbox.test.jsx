import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { CustomCheckbox } from '../../../components/ui/CustomCheckbox';

describe('CustomCheckbox', () => {
  it('muestra el label correctamente', () => {
    render(<CustomCheckbox label="Aceptar términos" checked={false} />);
    expect(screen.getByText('Aceptar términos')).toBeInTheDocument();
  });

  it('está marcado cuando `checked=true`', () => {
    render(<CustomCheckbox label="Checkbox marcado" checked={true} />);
    const input = screen.getByRole('checkbox');
    expect(input).toBeChecked();
  });

  it('no está marcado cuando `checked=false`', () => {
    render(<CustomCheckbox label="Checkbox no marcado" checked={false} />);
    const input = screen.getByRole('checkbox');
    expect(input).not.toBeChecked();
  });

  it('ejecuta `onChange` al hacer clic', () => {
    const handleChange = vi.fn();
    render(<CustomCheckbox label="Tildar" onChange={handleChange} checked={false} />);
    const input = screen.getByRole('checkbox');
    fireEvent.click(input);
    expect(handleChange).toHaveBeenCalledTimes(1);
  });
});
