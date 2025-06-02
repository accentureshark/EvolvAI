import React, { useContext } from 'react';
import { AppContext } from '../AppContext';

function ModalActuator() {
  const {
    modalVisible,
    modalOutput,
    modalLinks = [],
    setModalVisible
  } = useContext(AppContext);

  const onClose = () => setModalVisible(false);

  return (
      <div id="actuator-modal" className={modalVisible ? '' : 'hidden'} onClick={(e) => {
        if (e.target.id === 'actuator-modal') onClose();
      }}>
        <div>
          <button id="close-modal" onClick={onClose}>Cerrar</button>
          <pre id="actuator-output">{modalOutput}</pre>
          <ul>
            {modalLinks.map(([key, href]) => (
                <li key={key}><a href="#" onClick={() => console.log(`Abrir: ${href}`)}>{key}</a></li>
            ))}
          </ul>
        </div>
      </div>
  );
}

export default ModalActuator;
