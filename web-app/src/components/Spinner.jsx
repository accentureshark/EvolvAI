import React from 'react';

function Spinner({ show }) {
  if (!show) return null;

  return (
    <div id="spinner" className="spinner">
      <img src="logo.png" alt="Cargando..." className="spinner-logo" />
    </div>
  );
}

export default Spinner;