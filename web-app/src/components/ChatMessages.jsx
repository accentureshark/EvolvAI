import React, { useEffect, useRef, useState, useContext } from 'react';
import { AppContext } from '../AppContext';
import useChatEngine from '../hooks/useChatEngine';

const MAX_LEN = 200;

function ChatMessages() {
  const containerRef = useRef();
  const inputRef = useRef();
  const { messages, toast } = useContext(AppContext);
  const [customPrompt, setCustomPrompt] = useState('');
  const { sendMessage } = useChatEngine();

  useEffect(() => {
    const el = containerRef.current;
    if (el) el.scrollTop = el.scrollHeight;
  }, [messages]);

  const handleSubmit = (e) => {
    e.preventDefault();
    const input = inputRef.current.value;
    sendMessage(input, customPrompt);
    inputRef.current.value = '';
  };

  return (
    <div>
      <div id="messages" ref={containerRef}>
        {messages.map((msg, index) => {
          const isTruncated = msg.text.length > MAX_LEN;
          const shortText = isTruncated ? msg.text.slice(0, MAX_LEN) + "..." : msg.text;
          return (
            <div className={`message ${msg.who}`} key={index}>
              {msg.who === "bot" && <img className="avatar" src="shark-bot.png" alt="Bot" />}
              <div className="text">
                <span>{shortText}</span>
                {isTruncated && (
                  <a
                    href="#"
                    className="see-more"
                    data-expanded="false"
                    onClick={(e) => {
                      e.preventDefault();
                      const target = e.target;
                      const isExpanded = target.dataset.expanded === "true";
                      target.dataset.expanded = isExpanded ? "false" : "true";
                      target.textContent = isExpanded ? "Ver más" : "Ver menos";
                      target.previousSibling.textContent = isExpanded ? shortText : msg.text;
                    }}
                  >
                    Ver más
                  </a>
                )}
              </div>
            </div>
          );
        })}
      </div>
      <form id="input-area" onSubmit={handleSubmit}>
        <input id="user-input" type="text" ref={inputRef} placeholder="Escribí tu mensaje..." />
        <button type="submit">Enviar</button>
      </form>
    </div>
  );
}

export default ChatMessages;