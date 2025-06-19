import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { ActuatorModal } from '../../../components/ui/ActuatorModal';

global.fetch = vi.fn()

Object.assign(navigator, {
  clipboard: {
    writeText: vi.fn(),
  }
})

describe('ActuatorModal', () => {
  const onHide = vi.fn()

    beforeEach(() => {
        const mockClipboard = vi.fn().mockResolvedValue()
        Object.assign(navigator, {
        clipboard: {
            writeText: mockClipboard,
        },
        })
    })

    it('renderiza el modal cuando visible es true', () => {
        render(<ActuatorModal visible={true} onHide={onHide} />)
        expect(screen.getByText('Endpoints Actuator')).toBeInTheDocument()
    })

    it('no muestra el modal cuando visible es false', () => {
        render(<ActuatorModal visible={false} onHide={onHide} />)
        expect(screen.queryByText('Endpoints Actuator')).not.toBeInTheDocument()
    })

    it('hace fetch al hacer clic en un link y muestra el resultado', async () => {
    const mockData = { status: 'ok' }
    fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockData,
    })

    render(<ActuatorModal visible={true} onHide={onHide} />)

    fireEvent.click(screen.getByText('/actuator'))

    await waitFor(() => {
        expect(fetch).toHaveBeenCalledWith(expect.stringContaining('/actuator'))
        expect(screen.getByText((text) => text.includes('"status": "ok"'))).toBeInTheDocument()
    })
    })

    it('muestra mensaje de error si el fetch falla', async () => {
        fetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
        })

        render(<ActuatorModal visible={true} onHide={onHide} />)

        fireEvent.click(screen.getByText('/actuator'))

        await waitFor(() => {
        expect(screen.getByText(/Error al obtener/)).toBeInTheDocument()
        })
    })

    it('limpia el output cuando visible cambia a false', async () => {
        const { rerender } = render(<ActuatorModal visible={true} onHide={onHide} />)
        fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ test: 'data' }),
        })

        fireEvent.click(screen.getByText('/actuator'))

        await waitFor(() => {
        expect(screen.getByText(/test/)).toBeInTheDocument()
        })

        rerender(<ActuatorModal visible={false} onHide={onHide} />)
        rerender(<ActuatorModal visible={true} onHide={onHide} />)

        expect(screen.queryByText(/test/)).not.toBeInTheDocument()
    })

    it('llama a navigator.clipboard.writeText y muestra toast al copiar', async () => {
    const mockResponse = { msg: 'copiable' }

    fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse,
    })

    render(<ActuatorModal visible={true} onHide={onHide} />)

    fireEvent.click(screen.getByText('/actuator'))

    await waitFor(() => {
        expect(screen.getByText(/copiable/)).toBeInTheDocument()
    })

    const copyButton = screen.getByRole('button', { name: 'Copiar resultado' })
    fireEvent.click(copyButton)

    expect(navigator.clipboard.writeText).toHaveBeenCalledWith(
        JSON.stringify(mockResponse, null, 2)
    )
    })


    it('el botón de copiar está deshabilitado si no hay output', () => {
    render(<ActuatorModal visible={true} onHide={onHide} />)
    const copyButton = screen.getByRole('button', { name: 'Copiar resultado' })
    expect(copyButton).toBeDisabled()
    })

})
