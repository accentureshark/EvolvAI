
export const QuizDetail = ({ quiz }) => {
  if (!quiz) {
    return (
      <div className="quiz-detail-container quiz-detail-placeholder">
        <p>Selecciona un quiz para ver sus detalles.</p>
      </div>
    );
  }

  return (
    <div className="quiz-detail-container">
      <h3>{quiz.title}</h3>
      <ul>
        {quiz.questions.map((q, index) => (
          <li key={index}>{q.value}</li>
        ))}
      </ul>
    </div>
  );
};
