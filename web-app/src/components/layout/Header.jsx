import '../../styles/header.css';
import logo from '../../assets/shark-ia.png';
import { useState } from 'react';
import { ActuatorModal } from '../ui/ActuatorModal';

export const Header = () => {
  const [modalVisible, setModalVisible] = useState(false);

  return (
    <header className="header-container">
      <div className="header-logo">
        <img src={logo} alt="Logo" className="logo" height={80} width={100} />
        <h1 className="header-title">EvolvAI Chat</h1>
      </div>
      <div className="header-button">
        <button title='Estado del sistema' onClick={() => setModalVisible(true)} className="header-button-item">⚙️</button>
      </div>
      <ActuatorModal visible={modalVisible} onHide={() => setModalVisible(false)} />
    </header>
  )
}
