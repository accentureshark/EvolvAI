import { createContext, useContext, useState } from 'react';

const LogContext = createContext();

export const useLog = () => useContext(LogContext);

export const LogProvider = ({ children }) => {
    const [logs, setLogs] = useState([
        { type: 'info', message: '🖥️ Consola iniciada...' } 
    ]);

    const log = (message, type = "info") => {        
        setLogs((prevLogs) => [
            { message, type },
            ...prevLogs
        ]);
    }

    return (
        <LogContext.Provider value={{ logs, log }}>
            {children}
        </LogContext.Provider>
    );
};