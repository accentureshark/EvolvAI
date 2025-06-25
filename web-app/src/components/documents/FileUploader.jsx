import { useRef } from 'react';
import { FileUpload } from 'primereact/fileupload';
import { useDocument } from '../../contexts/DocumentContext';
import { useLog } from '../../contexts/LogContext';
import { useToast } from '../../contexts/ToastContext';

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

export const FileUploader = () => {
  const { fetchDocuments } = useDocument();
  const {log} = useLog();
  const toast = useToast();
  const uploaderRef = useRef(null);

  const handleUpload = async ({ files }) => {
    const file = files[0];
    const formData = new FormData();
    formData.append('file', file);
    log(`Subiendo archivo: ${files[0].name}`, "info");
    

    try {
      const res = await fetch(`${BACKEND_URL}/api/embeddings/upload`, {
        method: 'POST',
        body: formData,
      });

      if (!res.ok) {
        const errMsg = await res.text();
        throw new Error(errMsg);
      }

      await fetchDocuments();
      uploaderRef.current.clear();
      
      toast.current.show({
        severity: 'success',
        summary: 'Éxito',
        detail: `📁 ${file.name} subido correctamente`,
        life: 3000,
      });
    } catch (err) {
      log(`Error al subir el documento: ${err.message}`, "error");
      
      toast.current.show({
        severity: 'error',
        summary: 'Error',
        detail: `No se pudo subir: ${err.message}`,
        life: 3000,
      });
    }
  };

  return (
    <FileUpload
      chooseOptions={{
        icon: 'pi pi-upload',
        className: 'p-button-rounded p-button-primary',
      }}
      uploadOptions={{
        icon: 'pi pi-check',
        className: 'p-button-rounded p-button-success',
      }}
      cancelOptions={{
        icon: 'pi pi-times',
        className: 'p-button-rounded p-button-danger',
      }}
      customUpload
      ref={uploaderRef}
      uploadHandler={handleUpload}
      accept=".txt,.pdf,.json,.xml,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.html,.md"
      maxFileSize={10000000}
      emptyTemplate={<p>Sube acá tus documentos</p>}
    />
  );
}
