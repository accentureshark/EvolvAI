
import { Header } from '../components/layout/Header';
import { QuizPanel } from '../components/quiz/QuizPanel';
import { Card } from 'primereact/card';
import './Home.css';

const Home = () => {
  return (
    <div className="home-container">
      <Header />
      <main className="home-main">
        <QuizPanel />
      </main>
    </div>
  );
};

export default Home;

