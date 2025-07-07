import '../styles/login-form.css'
import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { Toast } from 'primereact/toast';
import { CustomCard } from '../components/ui/CustomCard';
import { CustomButton } from '../components/ui/CustomButton';
import { CustomCheckbox } from '../components/ui/CustomCheckbox';
import { InputField } from '../components/ui/InputField';

const Login = () => {
  const [credentials, setCredentials] = useState({ email: '', password: '' });
  const [rememberMe, setRememberMe] = useState(false);
  const { login, isLoading } = useAuth();
  const navigate = useNavigate();
  const toast = useRef(null);

  const handleInputChange = (field, value) => {
    setCredentials(prev => ({ ...prev, [field]: value }));
  };

  const handleRoleLogin = async (role) => {
    const { success } = await login(role);
    if (success) {
      toast.current.show({
        severity: 'success',
        summary: 'Login exitoso',
        detail: `Bienvenido ${role}`
      });
      if (role === 'Admin') {
        navigate('/home');
      } else {
        navigate('/user-dashboard');
      }
    }
  };

  return (
    <div className="login-container">
      <Toast ref={toast} />
      <CustomCard className="login-card">
        <div className="login-header">
          <h2>Iniciar Sesión</h2>
          <p>Ingresa tus credenciales para acceder</p>
        </div>
        
        <form onSubmit={(e) => e.preventDefault()} className="login-form">
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
            <div className="p-field-checkbox">
                <CustomCheckbox label="Recordame" inputId="rememberMe" onChange={e => setRememberMe(e.checked)} checked={rememberMe} />
            </div>
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
            onClick={() => alert("Inicio de sesión normal no implementado. Usa los botones de rol.")}
          />
        </form>

        <div className="login-footer">
          <p>O ingresa directamente como:</p>
          <div className="role-buttons">
            <CustomButton label="Admin" onClick={() => handleRoleLogin('Admin')} disabled={isLoading} severity="secondary" />
            <CustomButton label="Usuario" onClick={() => handleRoleLogin('User')} disabled={isLoading} severity="secondary" />
          </div>
        </div>
      </CustomCard>
    </div>
  );
};

export default Login;
