import '../../styles/message.css'

import { usePrompt } from '../../contexts/PromptContext';
import { useState } from 'react';
import { TextareaField } from '../ui/TextareaField';

export const PromptEditor = () => {
  const { prompt, setPrompt } = usePrompt();
  const [isActive, setIsActive] = useState(false);

  return (
    <div className="prompt-toggle">
      <div
        className="toggle-header"
        onClick={() => setIsActive(!isActive)}
        role="button"
        tabIndex={0}
      >
        <span>Prompt personalizado</span>
        <span className="arrow">{isActive ? '▼' : '▶'}</span>
      </div>
      {isActive && (
        <TextareaField
          name="custom-prompt"
          placeholder="Escribe tu prompt..."
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          rows={4}
          className='custom-prompt'
        />
      )}
    </div>
  );
};
