import '../../styles/log.css'
import { useLog } from '../../contexts/LogContext'
import { useState } from 'react';

export const LogDisplay = () => {

  const {logs} = useLog();
  const [collapsed, setCollapsed] = useState(false);

  return (
    <div className="log-container">
      <div className="log-header" onClick={() => setCollapsed(!collapsed)}>
        <span role="img" aria-label="log">💙 Logs</span>
        <button className="log-toggle-btn">{collapsed ? '▶' : '▼'}</button>
      </div>
      {!collapsed && (
        <div className="log-body">
          {logs.map((log, i) => (
            <div key={i} className={`log-entry ${log.type}`}>
              {log.type === 'error' ? '❌' : log.type === 'warn' ? '⚠️' : '💬'} {log.message}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
