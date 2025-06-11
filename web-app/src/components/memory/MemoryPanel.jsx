import '../../styles/memory.css';
import { useMemory } from '../../contexts/MemoryContext';

export const MemoryPanel = () => {
  const { memory, loading } = useMemory();

  return (
    <div className="memory-panel-container">
      <h4>Memoria</h4>
      {loading ? (
        <p>Cargando memoria...</p>
      ) : memory.length > 0 ? (
        <ul className="memory-list">
          {memory.map((msg, index) => (
            <li key={index}>
              <b>{msg.role}:</b> {msg.content}
            </li>
          ))}
        </ul>
      ) : (
        <p>(Vacía)</p>
      )}
    </div>
  );
}
