import '../../styles/message.css';

import { useRef, useState } from 'react';
import ReactMarkdown from 'react-markdown';
import botAvatar from '../../assets/shark-ia.png';
import userAvatar from '../../assets/logo.png';
import { CustomButton } from '../ui/CustomButton';
import { useToast } from '../../contexts/ToastContext'; 

export const MessageItem = ({ 
  text = "Esto es un texto por defecto", 
  type = "bot", 
  avatar, 
}) => {
  const [expanded, setExpanded] = useState(false);
  const toast = useToast();
  const MAX_LENGTH = 300;

  const currentAvatar = avatar || (type === 'bot' ? botAvatar : userAvatar);
  const avatarAlt = type === 'bot' ? 'Bot' : 'Usuario';

  const isLong = text.length > MAX_LENGTH;
  const displayText = expanded || !isLong ? text : text.slice(0, MAX_LENGTH) + '...';

  const handleCopy = () => {
    navigator.clipboard.writeText(text)
      .then(() => toast.current.show({ severity: 'info', summary: 'Copiado', detail: 'Mensaje copiado al portapapeles' }))
      .catch(() => toast.current.show({ severity: 'error', summary: 'Error', detail: 'No se pudo copiar el mensaje' }));
  };

  return (
    <>
      <li className={`message-item-container ${type}`}>
        <img src={currentAvatar} height={20} width={20} alt={avatarAlt} className="avatar" />
        <div className="text">
          <div className={`message-content ${type}`}>
            <ReactMarkdown>{displayText}</ReactMarkdown>
            {isLong && (
              <button className="show-more-btn" onClick={() => setExpanded(!expanded)}>
                {expanded ? 'Ver menos' : 'Ver más'}
              </button>
            )}
          </div>
          { text.trim().length > 0 && (
            <CustomButton
              icon="pi pi-copy"
              className="p-button-sm p-button-text copy-btn"
              onClick={handleCopy}
              tooltip="Copiar mensaje"
              ariaLabel="Copiar mensaje"
            />
          )}
        </div>
      </li>
    </>
  );
};
