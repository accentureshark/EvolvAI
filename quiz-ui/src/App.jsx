import QuizPlayer from "./components/QuizPlayer";

function App() {
    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold mb-4">Quiz de orientación Java Dev</h1>
            <QuizPlayer documentId="quiz-orientacion-java-dev" />
        </div>
    );
}

export default App;
