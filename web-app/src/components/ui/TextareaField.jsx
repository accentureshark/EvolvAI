import { InputTextarea } from 'primereact/inputtextarea';

export const TextareaField = ({
  name,
  placeholder,
  value,
  onChange,
  rows,
  autoResize = false,
  onKeyDown,
  className = '',
  disabled = false
}) => {
  return (
        <div className={`textarea-field ${className}`}>
          <InputTextarea
            name={name}
            placeholder={placeholder}
            value={value}
            onChange={onChange}
            rows={rows}
            autoResize={autoResize}
            onKeyDown={onKeyDown}
            disabled={disabled}
          />
        </div>
  )
}
