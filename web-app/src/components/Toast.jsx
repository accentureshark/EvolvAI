import React from 'react';

function Toast({ message, isError, onClose }) {
  const background = isError ? '#dc2626' : '#4c1d95';
  if (!message) return null;

  return (
    <div id="toast" style={{ background }}>
      <span id="toast-msg">{message}</span>
      <button id="toast-close" onClick={onClose}>✖</button>
    </div>
  );
}

export default Toast;