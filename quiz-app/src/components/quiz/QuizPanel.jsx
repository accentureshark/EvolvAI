import '../../styles/quiz.css'

import { useState, useEffect } from "react";
import { DataView } from 'primereact/dataview';
import { QuizModal } from "../ui/QuizModal";
import { QuizDetail } from "./QuizDetail";
import { CustomButton } from '../ui/CustomButton';
import { CustomCard } from '../ui/CustomCard';
import { quizService } from '../../services/quizService';

export const QuizPanel = () => {
  const [quizzes, setQuizzes] = useState([]);
  const [quizDetails, setQuizDetails] = useState({}); // To store detailed quiz data
  const [selectedQuiz, setSelectedQuiz] = useState(null);
  const [modalVisible, setModalVisible] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Load quizzes from backend
  useEffect(() => {
    loadQuizzes();
  }, []);

  const loadQuizzes = async () => {
    try {
      setLoading(true);
      setError(null);
      const quizIds = await quizService.getAllQuizzes();
      
      // Create quiz objects with documentId
      const quizList = quizIds.map(id => ({
        documentId: id,
        title: `Quiz ${id}`, // Placeholder title, will be updated when details are loaded
        steps: []
      }));
      
      setQuizzes(quizList);
    } catch (err) {
      console.error('Error loading quizzes:', err);
      setError('Error al cargar los quizzes');
      // Fallback to hardcoded data for development
      setQuizzes([
        { documentId: "1", title: "Onboarding de Nuevos Talentos", steps: [] },
        { documentId: "2", title: "Evaluación de Habilidades Técnicas", steps: [] },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const loadQuizDetails = async (documentId) => {
    try {
      // Check if we already have the details cached
      if (quizDetails[documentId]) {
        setSelectedQuiz(quizDetails[documentId]);
        return;
      }

      const quizData = await quizService.getQuizById(documentId);
      
      // Cache the quiz details
      setQuizDetails(prev => ({
        ...prev,
        [documentId]: quizData
      }));
      
      setSelectedQuiz(quizData);
    } catch (err) {
      console.error('Error loading quiz details:', err);
      setError('Error al cargar los detalles del quiz');
    }
  };

  const handleSaveQuiz = async (quizData) => {
    try {
      // Convert quiz-app format to backend format
      const quizDefinition = {
        documentId: `quiz_${Date.now()}`, // Generate unique ID
        tipo: "custom",
        tema: quizData.title,
        version: "1.0",
        steps: quizData.questions.map((question, index) => ({
          step: index + 1,
          id: `step_${index + 1}`,
          texto: question.value,
          opciones: question.options || ["Sí", "No"] // Default options if not provided
        }))
      };

      await quizService.uploadQuiz(quizDefinition);
      
      // Reload quizzes to show the new one
      await loadQuizzes();
      
      setModalVisible(false);
    } catch (err) {
      console.error('Error saving quiz:', err);
      setError('Error al guardar el quiz');
    }
  };

  const handleQuizSelect = (quiz) => {
    loadQuizDetails(quiz.documentId);
  };

  const itemTemplate = (quiz) => {
    const stepCount = quiz.steps ? quiz.steps.length : 0;
    const displayTitle = quiz.tema || quiz.title || quiz.documentId;
    
    return (
      <div className="quiz-list-item" onClick={() => handleQuizSelect(quiz)}>
        <div className="quiz-item-info">
          <h5>{displayTitle}</h5>
          <span>{stepCount} preguntas</span>
        </div>
        <CustomButton icon="pi pi-chevron-right" className="p-button-rounded p-button-text" severity="secondary" />
      </div>
    );
  };

return (
  <div className="quiz-panel-container">
    <CustomCard className="quiz-list-card">
      <div className="quiz-panel-header">
        <h2 style={{ display: 'inline-block', marginRight: '10px' }}>Listado de Quiz</h2>
        <CustomButton
          label="Crear Quiz"
          icon="pi pi-plus"
          severity="primary"
          onClick={() => setModalVisible(true)}
        />
      </div>
      
      {loading && <p>Cargando quizzes...</p>}
      {error && <p className="error-message">{error}</p>}
      
      {!loading && !error && (
        <DataView className="quiz-list" value={quizzes} itemTemplate={itemTemplate} />
      )}
    </CustomCard>
    
    <CustomCard title="Detalles del Quiz" className="quiz-detail-card">
      <QuizDetail quiz={selectedQuiz} />
    </CustomCard>
    
    <QuizModal
      visible={modalVisible}
      onHide={() => setModalVisible(false)}
      onSave={handleSaveQuiz}
    />
  </div>
);
}

