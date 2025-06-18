import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { TextareaField } from '../../../components/ui/TextareaField';

describe('TextareaField', () => {
  it('muestra el placeholder correctamente', () => {
    render(<TextareaField placeholder="Escribí algo..." value="" onChange={() => {}} />);
    const textarea = screen.getByPlaceholderText('Escribí algo...');
    expect(textarea).toBeInTheDocument();
  });

  it('muestra el valor correctamente', () => {
    render(<TextareaField value="Texto de prueba" onChange={() => {}} />);
    const textarea = screen.getByDisplayValue('Texto de prueba');
    expect(textarea).toBeInTheDocument();
  });

  it('ejecuta onChange al cambiar el texto', () => {
    const handleChange = vi.fn();
    render(<TextareaField value="" onChange={handleChange} />);
    const textarea = screen.getByRole('textbox');
    fireEvent.change(textarea, { target: { value: 'Nuevo texto' } });
    expect(handleChange).toHaveBeenCalledTimes(1);
  });

  it('tiene la cantidad de filas especificadas', () => {
    render(<TextareaField value="" onChange={() => {}} rows={5} />);
    const textarea = screen.getByRole('textbox');
    expect(textarea).toHaveAttribute('rows', '5');
  });
});
