import { createContext, useContext, useState, useEffect } from 'react';
import { useLog } from './LogContext';
const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;


const DocumentContext = createContext();

export const useDocument = () => useContext(DocumentContext);

export const DocumentProvider = ({ children }) => {
  const {log} = useLog();
  const [documents, setDocuments] = useState([]);
  const [selectedDocument, setSelectedDocument] = useState(null);

  const fetchDocuments = async () => {
    try {
      const res = await fetch(`${BACKEND_URL}/api/embeddings/documents`);

      if(!res.ok) {
        throw new Error(`Error al cargar documentos: ${res.status} ${res.statusText}`);
      }

      const data = await res.json();
      setDocuments(data);
      if (!selectedDocument && data.length > 0) {
        setSelectedDocument(data[0]);
      }
    } catch (err) {
      log("Error cargando documentos:" + err, "error");
    }
  };

  useEffect(() => {
    fetchDocuments();
  }, []);

  return (
    <DocumentContext.Provider
      value={{ documents, selectedDocument, setSelectedDocument, fetchDocuments }}
    >
      {children}
    </DocumentContext.Provider>
  );
};

export {DocumentContext};