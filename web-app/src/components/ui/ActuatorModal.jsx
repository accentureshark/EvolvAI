import { useEffect, useState } from "react";
import { CustomButton } from "./CustomButton";
import { Dialog } from "primereact/dialog";
import {useToast} from "../../contexts/ToastContext"
const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;
import "../../styles/modal.css";

const actuatorPaths = [
  { name: "actuator", path: "/actuator" },
  { name: "actuator/rag-status", path: "/actuator/rag-status" },
  { name: "actuator/rag-config", path: "/actuator/rag-config" }
];

export const ActuatorModal = ({ visible, onHide }) => {
  const [output, setOutput] = useState("");
  const toast = useToast();

  const loadActuator = async (path) => {
    try {
      const res = await fetch(`${BACKEND_URL}${path}`);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      setOutput(JSON.stringify(data, null, 2));
    } catch (err) {
      setOutput(`Error al obtener ${path}: ${err.message || err}`);
    }
  };

  const handleClick = (path) => {
    loadActuator(path);
  };

  const copyOutput = () => {
    navigator.clipboard.writeText(output)
      .then(() => toast.current.show({ severity: 'info', summary: 'Copiado', detail: 'Resultado copiado al portapapeles' }))
      .catch(() => toast.current.show({ severity: 'error', summary: 'Error', detail: 'No se pudo copiar el resultado' }));
  }

  useEffect(() => {
    if (!visible) {
      setOutput("");
    }
  }, [visible]);

return (
      <Dialog header="Endpoints Actuator" visible={visible} onHide={onHide} modal draggable={false} className="dialog">
        <ul className="actuator-list">
          {actuatorPaths.map(link => (
            <li key={link.path}>
              <a
                href="#"
                onClick={(e) => {
                  e.preventDefault();
                  handleClick(link.path);
                }}
                className="actuator-link"
              >
                /{link.name}
              </a>
            </li>
          ))}
        </ul>
        <div className="actuator-output-container">
          <CustomButton
            icon="pi pi-copy"
            className="p-button-sm p-button-text"
            style={{ position: 'absolute', top: 5, right: 8 }}
            onClick={copyOutput}
            disabled={!output}
            tooltip="Copiar resultado"
            ariaLabel="Copiar resultado"
          />
          <pre style={{ margin: 0, whiteSpace: 'pre-wrap', wordBreak: 'break-word' }} className="actuator-output">{output}</pre>
        </div>
      </Dialog>
  );
}
