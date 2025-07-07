import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { Button } from 'primereact/button';
import { InputText } from 'primereact/inputtext';
import { Checkbox } from 'primereact/checkbox';
import { Toast } from 'primereact/toast';
import { Card } from 'primereact/card'; // Import Card
import '../components/auth/LoginForm.css';

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
      <Card className="login-card">
        <div className="login-header">
          <h2>Iniciar Sesión</h2>
          <p>Ingresa tus credenciales para acceder</p>
        </div>
        
        <form onSubmit={(e) => e.preventDefault()} className="login-form">
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <InputText
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
            <InputText
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
                <Checkbox inputId="rememberMe" onChange={e => setRememberMe(e.checked)} checked={rememberMe} />
                <label htmlFor="rememberMe">Recordarme</label>
            </div>
            <a href="#" className="forgot-password">
              ¿Olvidaste tu contraseña?
            </a>
          </div>

          <Button
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
            <Button label="Admin" onClick={() => handleRoleLogin('Admin')} disabled={isLoading} severity="secondary" />
            <Button label="Usuario" onClick={() => handleRoleLogin('User')} disabled={isLoading} severity="secondary" />
          </div>
        </div>
      </Card>
    </div>
  );
};

export default Login;
