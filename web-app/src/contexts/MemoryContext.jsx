import { useLog } from "./LogContext";
import { createContext, useContext, useState, useEffect } from "react";
const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;


const MemoryContext = createContext();

export const useMemory = () => useContext(MemoryContext);

export const MemoryProvider = ({children}) => {
    const [memory, setMemory] = useState([]);
    const [loading, setLoading] = useState(true);
    const {log} = useLog();

    const fetchMemory = async () => {
        setLoading(true);
        try {
            const res = await fetch(`${BACKEND_URL}/chat-memory`);
            if (!res.ok) {
                throw new Error("No se pudo obtener la memoria");
            }

            const data = await res.json();
            setMemory(data);
            log("Memoria cargada correctamente", "info");
            
        } catch (err) {
            log("Error cargando memoria: " + err.message, "error");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchMemory();
    }, []);

    return (
        <MemoryContext.Provider value={{ memory, loading, fetchMemory }}>
            {children}
        </MemoryContext.Provider>
    );
};