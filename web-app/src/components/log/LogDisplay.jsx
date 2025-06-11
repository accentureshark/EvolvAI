import '../../styles/log.css'
import { useLog } from '../../contexts/LogContext'

export const LogDisplay = () => {

  const {logs} = useLog();
  
  return (
    <div className="log-container">
      <div className="log-header">
        <span role="img" aria-label="log">💙</span> Logs
      </div>
      <div className="log-body">
        {logs.map((log, i) => (
          <div key={i} className={`log-entry ${log.type}`}>
            {log.type === 'error' ? '❌' : log.type === 'warn' ? '⚠️' : '💬'} {log.message}
          </div>
        ))}
      </div>
    </div>
  );
};
