import { useNavigate } from 'react-router-dom';
import { DataView } from 'primereact/dataview';
import { Button } from 'primereact/button';
import { Header } from '../components/layout/Header';
import '../styles/user-dashboard.css';

const exampleQuizzes = [
  { id: 1, title: 'Quiz de Bienvenida', questions: [{id: 1, value: "Pregunta 1"}] },
  { id: 2, title: 'Evaluación de Desempeño', questions: [{id: 1, value: "Pregunta A"}] },
];

const UserDashboard = () => {
  const navigate = useNavigate();

  const itemTemplate = (quiz) => {
    return (
      <div className="user-dashboard-list-item" onClick={() => navigate(`/quiz/${quiz.id}`)}>
        <div className="quiz-item-info">
          <h5>{quiz.title}</h5>
        </div>
        <Button label="Comenzar Quiz" icon="pi pi-arrow-right" />
      </div>
    );
  };

  return (
    <div className="user-dashboard-container">
      <Header />
      <div className="user-dashboard-content">
        <h2>Quizes Disponibles</h2>
        <DataView value={exampleQuizzes} itemTemplate={itemTemplate} />
      </div>
    </div>
  );
};

export default UserDashboard;
