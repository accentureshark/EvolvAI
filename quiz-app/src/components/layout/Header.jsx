
import '../../styles/Header.css';
import { useAuth } from '../../contexts/AuthContext';
import { CustomButton } from '../ui/CustomButton';
import logo from '../../assets/shark-ia.png';


export const Header = () => {
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
  };

  return (
    <header className="header-container">
      <div className="header-logo">
        <img src={logo} alt="Logo" className="logo" height={80} width={100} />
        <h1 className="header-title">EvolvAI Quiz</h1>
      </div>
      <div className="header-button">
        <CustomButton
          label="Cerrar Sesión"
          icon="pi pi-sign-out"
          onClick={handleLogout}
          className="header-button-item"
          style={{
            background: 'rgba(168, 132, 250, 0.1)',
            border: '2px solid rgba(168, 132, 250, 0.3)',
            color: '#4c1d95',
            padding: '0.75rem 1.5rem',
            borderRadius: '8px',
            fontWeight: '500',
            fontSize: '14px'
          }}
        />
      </div>
    </header>
  )
}
