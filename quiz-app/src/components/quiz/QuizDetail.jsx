
export const QuizDetail = ({ quiz }) => {
  if (!quiz) {
    return (
      <div className="quiz-detail-container quiz-detail-placeholder">
        <p>Selecciona un quiz para ver sus detalles.</p>
      </div>
    );
  }

  // Handle both old format (questions) and new format (steps)
  const title = quiz.tema || quiz.title || quiz.documentId;
  const items = quiz.steps || quiz.questions || [];

  return (
    <div className="quiz-detail-container">
      <h3>{title}</h3>
      <div className="quiz-metadata">
        {quiz.tipo && <p><strong>Tipo:</strong> {quiz.tipo}</p>}
        {quiz.version && <p><strong>Versión:</strong> {quiz.version}</p>}
        {quiz.documentId && <p><strong>ID:</strong> {quiz.documentId}</p>}
      </div>
      
      <h4>Preguntas ({items.length})</h4>
      <ul>
        {items.map((item, index) => (
          <li key={item.id || index}>
            <div className="question-item">
              <strong>Pregunta {item.step || index + 1}:</strong>
              <p>{item.texto || item.value}</p>
              {item.opciones && (
                <div className="question-options">
                  <strong>Opciones:</strong>
                  <ul>
                    {item.opciones.map((option, optIndex) => (
                      <li key={optIndex}>{option}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
};
