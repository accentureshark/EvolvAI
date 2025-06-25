import { useDocument } from '../../contexts/DocumentContext.jsx';
import {FileUploader} from './FileUploader.jsx';
import {CustomButton} from '../ui/CustomButton.jsx'

export const DocumentList = () => {
  const {documents, selectedDocument, setSelectedDocument, removeDocumentById} = useDocument();

  const onRemoveDocument = async () => {
    if (!selectedDocument) {
      return;
    } 

    const confirm = window.confirm(`¿Eliminar "${selectedDocument}"?`);

    if (!confirm) {
      return;
    } 

    await removeDocumentById(selectedDocument);
  };

  return (
    <div className='document-list-container'>
      {documents.length > 0 && (
      <>
        <div className='document-list-header'>
          <p className='document-uploaded-title'>Documentos cargados</p>
          <CustomButton
              icon="pi pi-trash"
              className="p-button-danger p-button-sm"
              onClick={onRemoveDocument}
              disabled={!selectedDocument}
              tooltip="Eliminar documento seleccionado"
              ariaLabel="Eliminar documento"
            />
        </div>
        <select
          className="document-select"
          value={selectedDocument || ''}
          onChange={(e) => setSelectedDocument(e.target.value)}
        >
          {documents.map(docId => (
            <option key={docId} value={docId}>
              {docId}
            </option>
          ))}
        </select>
      </>
      )}
      <FileUploader  />
    </div>
  );
};
