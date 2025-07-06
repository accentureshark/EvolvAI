
import { Header } from '../components/layout/Header';
import { QuizPanel } from '../components/quiz/QuizPanel';
import './Home.css';

const Home = () => {
  return (
    <div className="home-container">
      <Header />
      
      <main className="home-main">
        <div className="welcome-card">
          <h2>¡Bienvenido a tu Dashboard!</h2>
          <p>Has iniciado sesión correctamente.</p>
        </div>
        <QuizPanel />
      </main>
    </div>
  );
};

export default Home;
