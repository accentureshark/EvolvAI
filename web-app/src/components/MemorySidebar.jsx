import React from 'react';

function MemorySidebar({ memory }) {
  return (
    <div id="sidebar-memory">
      <h4>Memoria</h4>
      {typeof memory === 'string' ? (
        <p>{memory}</p>
      ) : Array.isArray(memory) ? (
        <ul>{memory.map((item, idx) => <li key={idx}>{item}</li>)}</ul>
      ) : (
        <p>(Vacía)</p>
      )}
    </div>
  );
}

export default MemorySidebar;