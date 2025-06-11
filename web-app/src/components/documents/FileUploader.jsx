import { FileUpload } from 'primereact/fileupload';
import { useDocument } from '../../contexts/DocumentContext';
import { useLog } from '../../contexts/LogContext';

export const FileUploader = () => {
  const { fetchDocuments } = useDocument();
  const {log} = useLog();

  const handleUpload = async ({ files }) => {
    const formData = new FormData();
    formData.append('file', files[0]);

    try {
      const res = await fetch('http://localhost:8081/api/embeddings/upload', {
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
      multiple
      uploadHandler={handleUpload}
      accept=".txt,.pdf,.json,.xml"
      maxFileSize={10000000}
      emptyTemplate={<p className="m-0">Sube acá tus documentos</p>}
    />
  );
}
