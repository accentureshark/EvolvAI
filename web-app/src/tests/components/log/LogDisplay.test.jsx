import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { LogDisplay } from '../../../components/log/LogDisplay';
import * as LogContext from '../../../contexts/LogContext';

describe('LogDisplay', () => {
    const mockLogs = [
        { type: 'info', message: 'Mensaje informativo' },
        { type: 'warn', message: 'Mensaje de advertencia' },
        { type: 'error', message: 'Mensaje de error' },
    ]

    beforeEach(() => {
        // Espiar el hook y devolver logs mockeados
        vi.spyOn(LogContext, 'useLog').mockReturnValue({ logs: mockLogs })
    })

    it('renderiza los logs con los emojis correctos', () => {
        render(<LogDisplay />)

        expect(screen.getByText('💬 Mensaje informativo')).toBeInTheDocument()
        expect(screen.getByText('⚠️ Mensaje de advertencia')).toBeInTheDocument()
        expect(screen.getByText('❌ Mensaje de error')).toBeInTheDocument()
    })

    it('colapsa y expande los logs al hacer clic en el header', () => {
    render(<LogDisplay />)

    // Logs visibles inicialmente (buscar por texto parcial con función)
    expect(
        screen.getByText((content) => content.includes('Mensaje informativo'))
    ).toBeInTheDocument()

    // Click en el header
    fireEvent.click(screen.getByText('💙 Logs'))

    // Logs colapsados (ya no debería encontrar ese texto)
    expect(
        screen.queryByText((content) => content.includes('Mensaje informativo'))
    ).not.toBeInTheDocument()

    // Click de nuevo para expandir
    fireEvent.click(screen.getByText('💙 Logs'))
    expect(
        screen.getByText((content) => content.includes('Mensaje informativo'))
    ).toBeInTheDocument()
    })
})
