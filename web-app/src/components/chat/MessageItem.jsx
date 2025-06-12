import '../../styles/message.css';

import { useState } from 'react';
import botAvatar from '../../assets/shark-ia.png';
import userAvatar from '../../assets/logo.png';

export const MessageItem = ({ 
  text = "Esto es un texto por defecto", 
  type = "bot", 
  avatar, 
}) => {

  const [expanded, setExpanded] = useState(false);
  const MAX_LENGTH = 300;

  const currentAvatar = avatar || (type === 'bot' ? botAvatar : userAvatar);
  const avatarAlt = type === 'bot' ? 'Bot' : 'Usuario';

  const isLong = text.length > MAX_LENGTH;
  const displayText = expanded || !isLong ? text : text.slice(0, MAX_LENGTH) + '...';

  return (
    <li className={`message-item-container ${type}`}>
      <img src={currentAvatar} height={20} width={20} alt={avatarAlt} className="avatar" />
      <div className="text">
        {displayText}
        {isLong && (
          <button className="show-more-btn" onClick={() => setExpanded(!expanded)}>
            {expanded ? 'Ver menos' : 'Ver más'}
          </button>
        )}
      </div>
    </li>
  );

}
