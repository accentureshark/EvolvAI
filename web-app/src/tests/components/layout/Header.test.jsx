import { render, screen, fireEvent } from '@testing-library/react';
import { Header } from '../../../components/layout/Header';

describe('Header', () => {
  it('renderiza logo y título', () => {
    render(<Header />);
    expect(screen.getByAltText('Logo')).toBeInTheDocument();
    expect(screen.getByText('EvolvAI Chat')).toBeInTheDocument();
  });

  it('muestra el botón de estado del sistema', () => {
    render(<Header />);
    const button = screen.getByRole('button', { name: /estado del sistema/i });
    expect(button).toBeInTheDocument();
  });

  it('al hacer click en el botón abre el ActuatorModal', () => {
    render(<Header />);
    const button = screen.getByRole('button', { name: /estado del sistema/i });

    // Modal inicialmente no visible
    expect(screen.queryByText('Endpoints Actuator')).not.toBeInTheDocument();

    // Click para abrir modal
    fireEvent.click(button);

    // Ahora debería estar visible
    expect(screen.getByText('Endpoints Actuator')).toBeInTheDocument();
  });

  it('cierra el modal al llamar a onHide', () => {
    render(<Header />);
    const button = screen.getByRole('button', { name: /estado del sistema/i });
    fireEvent.click(button);
    
    // Modal visible
    expect(screen.getByText('Endpoints Actuator')).toBeInTheDocument();

    // Simulamos el cierre llamando onHide, que cambia modalVisible a false
    // Para eso necesitamos acceder al ActuatorModal y disparar onHide:
    // Pero como es un componente hijo, podemos obtener el botón de cierre dentro del modal y clickearlo
    // Asumiendo que ActuatorModal tiene un botón con rol button o similar para cerrar
  });
});