// Modularización para futura migración a React
import './utils.js';
import './api.js';
import './messages.js';
import './fileUpload.js';
import './memory.js';
import './prompt.js';
import './websocket.js';
import './modal.js';
import './main.js';

// Si necesitas exponer funciones globales para el HTML (por ejemplo, loadActuator)
import { loadActuator, copyActuatorOutput } from './modal.js';
window.loadActuator = loadActuator;
window.copyActuatorOutput = copyActuatorOutput;

// El resto de la lógica está ahora en los módulos correspondientes.
