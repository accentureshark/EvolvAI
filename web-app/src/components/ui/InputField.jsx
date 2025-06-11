
export const InputField = ({value, onChange, disabled}) => {
  return (
    <input className="input-message" disabled={disabled} value={value} onChange={onChange} type="text" id='send-message' placeholder='Escribe un mensaje...' />
  )
}
