import React, { useRef, useState, useContext } from 'react';
import { BACKEND_URL } from '../api/api';
import { AppContext } from '../AppContext';

function FileUpload() {
  const fileInputRef = useRef();
  const [fileName, setFileName] = useState('');
  const [showIcon, setShowIcon] = useState(false);
  const { showToast, setLogs } = useContext(AppContext);

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    setFileName(file?.name || '');
    setShowIcon(!!file);
  };

  const handleUpload = async () => {
    const file = fileInputRef.current.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("file", file);

    try {
      const res = await fetch(`${BACKEND_URL}/api/inference/upload`, {
        method: "POST",
        body: formData
      });
      const text = await res.text();
      if (!res.ok) throw new Error(text);
      setLogs(prev => [...prev, `✅ Archivo "${file.name}" subido correctamente`]);
      setFileName('');
      setShowIcon(false);
      fileInputRef.current.value = '';
    } catch (err) {
      setLogs(prev => [...prev, "❌ Error al subir archivo: " + err.message]);
      showToast("Error al subir archivo", true);
    }
  };

  return (
    <div id="file-upload-panel">
      <label id="file-label" onClick={() => fileInputRef.current.click()}>📎</label>
      <input id="file-input" type="file" hidden ref={fileInputRef} onChange={handleFileChange} />
      <ul id="file-list">{fileName && <li>{fileName}</li>}</ul>
      {showIcon && (
        <span id="file-save-icon" title="Guardar archivo" onClick={handleUpload}>💾</span>
      )}
    </div>
  );
}

export default FileUpload;