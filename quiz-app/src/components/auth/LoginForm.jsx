
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { CustomButton } from '../ui/CustomButton';
import { InputField } from '../ui/InputField';
import { CustomCheckbox } from '../ui/CustomCheckbox';
import { Toast } from 'primereact/toast';
import { useRef } from 'react';
import './LoginForm.css';

export const LoginForm = () => {
  const [credentials, setCredentials] = useState({
    email: '',
    password: ''
  });
  const [rememberMe, setRememberMe] = useState(false);
  const { login, isLoading } = useAuth();
  const navigate = useNavigate();
  const toast = useRef(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!credentials.email || !credentials.password) {
      toast.current.show({
        severity: 'warn',
        summary: 'Campos requeridos',
        detail: 'Por favor completa todos los campos'
      });
      return;
    }

    const result = await login(credentials);
    
    if (result.success) {
      toast.current.show({
        severity: 'success',
        summary: 'Login exitoso',
        detail: 'Bienvenido de vuelta'
      });
      navigate('/home');
    } else {
      toast.current.show({
        severity: 'error',
        summary: 'Error de autenticación',
        detail: result.error
      });
    }
  };

  const handleInputChange = (field, value) => {
    setCredentials(prev => ({
      ...prev,
      [field]: value
    }));
  };

  return (
    <div className="login-container">
      <Toast ref={toast} />
      <div className="login-card">
        <div className="login-header">
          <h2>Iniciar Sesión</h2>
          <p>Ingresa tus credenciales para acceder</p>
        </div>
        
        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <InputField
              id="email"
              type="email"
              placeholder="Ingresa tu email"
              value={credentials.email}
              onChange={(e) => handleInputChange('email', e.target.value)}
              className="login-input"
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Contraseña</label>
            <InputField
              id="password"
              type="password"
              placeholder="Ingresa tu contraseña"
              value={credentials.password}
              onChange={(e) => handleInputChange('password', e.target.value)}
              className="login-input"
            />
          </div>

          <div className="form-options">
            <CustomCheckbox
              checked={rememberMe}
              onChange={(e) => setRememberMe(e.checked)}
              label="Recordarme"
              className="remember-checkbox"
            />
            <a href="#" className="forgot-password">
              ¿Olvidaste tu contraseña?
            </a>
          </div>

          <CustomButton
            type="submit"
            label={isLoading ? "Ingresando..." : "Iniciar Sesión"}
            disabled={isLoading}
            className="login-button"
            icon={isLoading ? "pi pi-spin pi-spinner" : "pi pi-sign-in"}
          />
        </form>

        <div className="login-footer">
          <p>Credenciales de prueba:</p>
          <p><strong>Email:</strong> admin@example.com</p>
          <p><strong>Contraseña:</strong> password</p>
        </div>
      </div>
    </div>
  );
};
