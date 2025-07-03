import { useEffect, useState } from "react";
import axios from "axios";

export function useQuiz(documentId) {
    const [quiz, setQuiz] = useState(null);
    const [stepIndex, setStepIndex] = useState(0);
    const [answers, setAnswers] = useState({});
    const [finished, setFinished] = useState(false);
    const [result, setResult] = useState(null);

    useEffect(() => {
        axios.get(`/api/quiz/${documentId}`)
            .then(res => setQuiz(res.data))
            .catch(console.error);
    }, [documentId]);

    const currentStep = quiz?.steps?.[stepIndex] || null;

    const answerCurrent = (answer) => {
        if (!currentStep) return;

        setAnswers(prev => ({ ...prev, [currentStep.id]: answer }));

        if (stepIndex + 1 < quiz.steps.length) {
            setStepIndex(prev => prev + 1);
        } else {
            setFinished(true);
            submitAnswers(); // enviar al backend
        }
    };

    const submitAnswers = async () => {
        try {
            const response = await axios.post(`/api/quiz/response`, {
                documentId,
                usuario: "usuario@ejemplo.com", // reemplazar por usuario real
                respuestas: answers
            });
            setResult(response.data);
        } catch (err) {
            console.error("Error al enviar respuestas", err);
        }
    };

    const reset = () => {
        setStepIndex(0);
        setAnswers({});
        setFinished(false);
        setResult(null);
    };

    return {
        quiz,
        currentStep,
        stepIndex,
        totalSteps: quiz?.steps?.length || 0,
        answers,
        finished,
        result,
        answerCurrent,
        reset
    };
}
