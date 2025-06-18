import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { InputField } from '../../../components/ui/InputField';

describe('InputField', () => {
  it('muestra el placeholder correctamente', () => {
    render(<InputField placeholder="Escribí tu nombre" value="" onChange={() => {}} />);
    const input = screen.getByPlaceholderText('Escribí tu nombre');
    expect(input).toBeInTheDocument();
  });

  it('muestra el valor correctamente', () => {
    render(<InputField value="Juan" onChange={() => {}} />);
    const input = screen.getByDisplayValue('Juan');
    expect(input).toBeInTheDocument();
  });

  it('ejecuta onChange al cambiar el valor', () => {
    const handleChange = vi.fn();
    render(<InputField value="" onChange={handleChange} />);
    const input = screen.getByRole('textbox');
    fireEvent.change(input, { target: { value: 'Nuevo valor' } });
    expect(handleChange).toHaveBeenCalledTimes(1);
  });

  it('está deshabilitado si `disabled=true`', () => {
    render(<InputField value="" onChange={() => {}} disabled />);
    const input = screen.getByRole('textbox');
    expect(input).toBeDisabled();
  });
});
