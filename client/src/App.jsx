// src/App.jsx

import React from 'react';
import './App.css';
import Navigator from './components/Navigator';
import { UserProvider } from './UserContext';

function App() {
  return (
    <UserProvider>
      <Navigator />
    </UserProvider>
  );
}

export default App;
