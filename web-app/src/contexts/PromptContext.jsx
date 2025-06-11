import { createContext, useContext, useEffect, useState } from 'react';
import {useLog} from './LogContext';
const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

const PromptContext = createContext();

export const PromptProvider = ({ children }) => {
    const [prompt, setPrompt] = useState("");
    const { log } = useLog();

    useEffect(() => {
        const fetchDefaultPrompt = async () => {
            try {
                const res = await fetch(`${BACKEND_URL}/api/llm/prompt`);
                if(!res.ok) {
                    throw new Error('No se pudo obtener el prompt por defecto');
                }

                const text = await res.text();
                setPrompt(text);
                log('Prompt por defecto cargado correctamente', 'info');
            } catch (err) {
                log(`Error al cargar el prompt por defecto: ${err.message}`, 'error');
            }
        };

        fetchDefaultPrompt();
    }, []);

    return (
        <PromptContext.Provider value={{ prompt, setPrompt }}>
            {children}
        </PromptContext.Provider>
    );
};

export const usePrompt = () => useContext(PromptContext);