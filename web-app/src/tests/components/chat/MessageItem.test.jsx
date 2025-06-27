import { render, screen, fireEvent } from '@testing-library/react';
import { MessageItem } from '../../../components/chat/MessageItem';
import botAvatar from '../../../assets/shark-ia.png';
import userAvatar from '../../../assets/logo.png';

describe('MessageItem', () => {
  it('renderiza mensaje de usuario con avatar por defecto', () => {
    render(<MessageItem text="Hola mundo" type="user" />);
    
    expect(screen.getByText('Hola mundo')).toBeInTheDocument();
    const avatar = screen.getByAltText('Usuario');
    expect(avatar).toHaveAttribute('src', userAvatar);
  });

  it('renderiza mensaje de bot con avatar personalizado', () => {
    const customAvatar = 'https://example.com/bot.png';
    render(<MessageItem text="Soy el bot" type="bot" avatar={customAvatar} />);

    expect(screen.getByText('Soy el bot')).toBeInTheDocument();
    const avatar = screen.getByAltText('Bot');
    expect(avatar).toHaveAttribute('src', customAvatar);
  });
});
