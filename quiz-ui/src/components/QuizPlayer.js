import { useQuiz } from "../hooks/useQuiz";

export default function QuizPlayer({ documentId }) {
    const {
        quiz,
        currentStep,
        stepIndex,
        totalSteps,
        answers,
        finished,
        result,
        answerCurrent,
        reset
    } = useQuiz(documentId);

    if (!quiz) return <p>Cargando quiz...</p>;

    if (finished) {
        return (
            <div className="p-4">
                <h2 className="text-xl font-bold mb-2">¡Quiz completado!</h2>
                <p className="mb-4">Tus respuestas:</p>
                <pre className="bg-gray-100 p-2 rounded text-sm mb-4">{JSON.stringify(answers, null, 2)}</pre>
                {result && (
                    <>
                        <p className="font-semibold">Resultado inferido:</p>
                        <div className="bg-green-100 border-l-4 border-green-500 p-4 my-2">
                            {result.answer}
                        </div>
                    </>
                )}
                <button onClick={reset} className="mt-4 bg-blue-600 text-white px-4 py-2 rounded">Volver a empezar</button>
            </div>
        );
    }

    return (
        <div className="p-4 border rounded shadow-md max-w-xl mx-auto">
            <h3 className="text-lg font-semibold mb-2">Pregunta {stepIndex + 1} de {totalSteps}</h3>
            <p className="text-md mb-4">{currentStep.texto}</p>
            <div className="space-y-2">
                {currentStep.opciones.map((op, i) => (
                    <button
                        key={i}
                        className="w-full border p-2 rounded hover:bg-gray-100 text-left"
                        onClick={() => answerCurrent(op)}
                    >
                        {op}
                    </button>
                ))}
            </div>
        </div>
    );
}
