import React, { useState, useEffect, useContext } from 'react';
import { AppContext } from '../AppContext';
import useChatEngine from '../hooks/useChatEngine';

function PromptPanel() {
  const [collapsed, setCollapsed] = useState(() => {
    return localStorage.getItem("promptCollapsed") !== "false";
  });
  const [prompt, setPrompt] = useState('');
  const { loadPrompt } = useChatEngine();
  const { showToast } = useContext(AppContext);

  useEffect(() => {
    loadPrompt().then(setPrompt).catch(err => {
      showToast('Error cargando prompt', true);
    });
  }, [loadPrompt, showToast]);

  const togglePanel = () => {
    setCollapsed((prev) => {
      localStorage.setItem("promptCollapsed", !prev);
      return !prev;
    });
  };

  return (
    <div id="prompt-panel" className={collapsed ? "collapsed" : ""}>
      <div id="prompt-header" onClick={togglePanel}>
        Prompt {collapsed ? "▲" : "▼"}
      </div>
      {!collapsed && (
        <textarea id="custom-prompt" value={prompt} onChange={(e) => setPrompt(e.target.value)} />
      )}
    </div>
  );
}

export default PromptPanel;