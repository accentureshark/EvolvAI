import '../../styles/quiz.css'

import { useState } from "react";
import { DataView } from 'primereact/dataview';
import { Button } from "primereact/button";
import { QuizModal } from "../ui/QuizModal";
import { QuizDetail } from "./QuizDetail";
import { Card } from 'primereact/card'; // Import Card

export const QuizPanel = () => {
  const [quizzes, setQuizzes] = useState([
    { id: 1, title: "Onboarding de Nuevos Talentos", questions: [{id: 1, value: "Pregunta 1"}, {id: 2, value: "Pregunta 2"}] },
    { id: 2, title: "Evaluación de Habilidades Técnicas", questions: [{id: 1, value: "Pregunta A"}] },
  ]);
  const [selectedQuiz, setSelectedQuiz] = useState(null);
  const [modalVisible, setModalVisible] = useState(false);

  const handleSaveQuiz = (quizData) => {
    const newQuiz = {
      ...quizData,
      id: quizzes.length + 1, // Simple ID generation
    };
    setQuizzes([...quizzes, newQuiz]);
    setModalVisible(false);
  };

  const itemTemplate = (quiz) => {
    return (
      <div className="quiz-list-item" onClick={() => setSelectedQuiz(quiz)}>
        <div className="quiz-item-info">
          <h5>{quiz.title}</h5>
          <span>{quiz.questions.length} preguntas</span>
        </div>
        <Button icon="pi pi-chevron-right" className="p-button-rounded p-button-text" severity="secondary" />
      </div>
    );
  };

return (
  <div className="quiz-panel-container">
    <Card className="quiz-list-card">
      <div className="quiz-panel-header">
        <h2 style={{ display: 'inline-block', marginRight: '10px' }}>Listado de Quiz</h2>
        <Button
          label="Crear Quiz"
          icon="pi pi-plus"
          severity="primary"
          onClick={() => setModalVisible(true)}
        />
      </div>
      <DataView className="quiz-list" value={quizzes} itemTemplate={itemTemplate} />
    </Card>
    <Card title="Detalles del Quiz" className="quiz-detail-card">
      <QuizDetail quiz={selectedQuiz} />
    </Card>
    <QuizModal
      visible={modalVisible}
      onHide={() => setModalVisible(false)}
      onSave={handleSaveQuiz}
    />
  </div>
);
}

