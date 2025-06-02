export function log(msg, setLogs) {
  setLogs(prev => [...prev, msg]);
}