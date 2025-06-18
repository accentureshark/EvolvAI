import { FileUpload } from 'primereact/fileupload';
import { useDocument } from '../../contexts/DocumentContext';
import { useLog } from '../../contexts/LogContext';

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

export const FileUploader = () => {
  const { fetchDocuments } = useDocument();
  const {log} = useLog();

  const handleUpload = async ({ files }) => {
    const formData = new FormData();
    formData.append('file', files[0]);
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
    } catch (err) {
      log(`Error al subir el documento: ${err.message}`, "error");
      console.error("Error al subir documento:", err);
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
      uploadHandler={handleUpload}
      accept=".txt,.pdf,.json,.xml,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.html,.md"
      maxFileSize={10000000}
      emptyTemplate={<p>Sube acá tus documentos</p>}
    />
  );
}
