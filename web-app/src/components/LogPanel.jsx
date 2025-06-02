import React, { useContext } from 'react';
import { AppContext } from '../AppContext';

function LogPanel() {
    const { logs = [] } = useContext(AppContext);

    return (
        <div id="log-panel">
            <div id="log-toggle">▼ Logs</div>
            <pre id="log-entries">
        {logs.map((msg, idx) => (
            <div key={idx}>{msg}</div>
        ))}
      </pre>
        </div>
    );
}

export default LogPanel;
