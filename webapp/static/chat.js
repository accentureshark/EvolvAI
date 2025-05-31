// chat.js
// Agrupa e importa todos los módulos de la app (simula un entrypoint para un futuro React)

import './utils.js';
import './api.js';
import './messages.js';
import './fileUpload.js';
import './memory.js';
import './prompt.js';
import './websocket.js';
import './modal.js';
import './main.js';

// Exponer funciones globales para el HTML, si se usan en chat.html
import { loadActuator, copyActuatorOutput } from './modal.js';
window.loadActuator = loadActuator;
window.copyActuatorOutput = copyActuatorOutput;
