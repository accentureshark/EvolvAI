import { InputTextarea } from 'primereact/inputtextarea';

export const TextareaField = ({name, placeholder, value, onChange, rows}) => {
  return (
      <InputTextarea name={name} placeholder={placeholder} value={value} onChange={onChange} rows={rows} />
  )
}
