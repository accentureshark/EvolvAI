import { createContext, useContext, useRef } from "react";
import { Toast } from "primereact/toast";

const ToastContext = createContext();

export const ToastProvider = ({ children }) => {
  const toastRef = useRef(null);

  return (
    <ToastContext.Provider value={toastRef}>
      <Toast ref={toastRef} position="top-right" />
      {children}
    </ToastContext.Provider>
  );
};

export const useToast = () => useContext(ToastContext);
