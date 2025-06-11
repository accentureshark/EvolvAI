import '../../styles/message.css';

import botAvatar from '../../assets/shark-ia.png';
import userAvatar from '../../assets/logo.png';

export const MessageItem = ({ 
  text = "Esto es un texto por defecto", 
  type = "bot", 
  avatar, 
}) => {
    
  const currentAvatar = avatar || (type === 'bot' ? botAvatar : userAvatar);
  const avatarAlt = type === 'bot' ? 'Bot' : 'Usuario';

  return (
    <li className={`message-item-container ${type}`}>
      <img src={currentAvatar} height={20} width={20} alt={avatarAlt} className="avatar" />
      <div className="text">{text}</div>
    </li>
  );

}
