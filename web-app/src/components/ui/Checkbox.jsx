export const Checkbox = ({onChange, checked}) => {
  return (
    <div className="checkbox-container">
      <input type="checkbox" id="checkbox" onChange={onChange} checked={checked} className="checkbox" />
      <label htmlFor="checkbox" className="checkbox-label">
        Streaming
      </label>
    </div>
  )
}
