import { createContext, useContext, useState, useEffect } from 'react';
import { useLog } from './LogContext';
import { useToast } from './ToastContext';
const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;


const DocumentContext = createContext();

export const useDocument = () => useContext(DocumentContext);

export const DocumentProvider = ({ children }) => {
  const {log} = useLog();
  const toast = useToast();
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

  const removeDocumentById = async (id) => {
    try {
      const res = await fetch(`${BACKEND_URL}/api/embeddings/remove/${encodeURIComponent(id)}`, {
        method: 'DELETE',
      });

      if (!res.ok) {
        throw new Error(`Error al eliminar: ${res.status} ${res.statusText}`);
      } 

      toast.current.show({
        severity: 'success',
        summary: 'Éxito',
        detail: `📁 Documento ${id} eliminado correctamente`,
        life: 3000,
      });

      log(`Documento "${id}" eliminado correctamente.`);

      const fetchRes = await fetch(`${BACKEND_URL}/api/embeddings/documents`);
      const data = await fetchRes.json();

      setDocuments(data);

      if (data.length > 0) {
        setSelectedDocument(data[0]);
      } else {
        setSelectedDocument(null);
      }

    } catch (err) {
      toast.current.show({
        severity: 'error',
        summary: 'Error',
        detail: `No se pudo eliminar: ${err.message}`,
        life: 3000,
      });
      log(`Error eliminando documento: ${err.message}`, "error");
    }
  };

  useEffect(() => {
    fetchDocuments();
  }, []);

  return (
    <DocumentContext.Provider
      value={{ documents, selectedDocument, setSelectedDocument, fetchDocuments, removeDocumentById }}
    >
      {children}
    </DocumentContext.Provider>
  );
};

export {DocumentContext};