import '../../styles/typing-indicator.css';

export const TypingIndicator = () => {
  return (
    <li className="message-item bot typing-indicator">
      <span className="dot"></span>
      <span className="dot"></span>
      <span className="dot"></span>
    </li>
  );
};