import React from 'react';
import ReactDOM from 'react-dom/client';
import { SessionProvider } from './context/SessionContext';
import { StockProvider } from './context/StockContext';
import App from './App';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <SessionProvider>
      <StockProvider>
        <App />
      </StockProvider>
    </SessionProvider>
  </React.StrictMode>
);
