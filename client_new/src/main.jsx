import React from 'react';
import ReactDOM from 'react-dom/client';
import { AuthProvider } from './context/AuthContext';
import { StockContext, StockProvider } from './context/StockContext';
import App from './App';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <AuthProvider>
      <StockProvider>
        <App />
      </StockProvider>
    </AuthProvider>
  </React.StrictMode>
);