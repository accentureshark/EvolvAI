import { useDocument } from '../../contexts/DocumentContext.jsx';
import {FileUploader} from './FileUploader.jsx';

export const DocumentList = () => {
  const {documents, selectedDocument, setSelectedDocument} = useDocument();

  return (
    <div className='document-list-container'>
      <FileUploader  />
      {documents.length > 0 && (
        <>
          <p className='document-uploaded-title'>Documentos cargados</p>
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
    </div>
  );
};
