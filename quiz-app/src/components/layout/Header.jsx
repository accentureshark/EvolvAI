
import '../../styles/Header.css';
import logo from '../../assets/shark-ia.png';
import { useAuth } from '../../contexts/AuthContext';
import { CustomButton } from '../ui/CustomButton';


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
        />
      </div>
    </header>
  )
}
