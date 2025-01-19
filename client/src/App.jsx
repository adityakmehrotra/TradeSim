import React, { useState, useEffect } from 'react';
import './App.css';
import Navigator from './components/Navigator';
import { UserProvider } from './UserContext';

function App() {
  const [activeTab, setActiveTab] = useState("TradeSim");

  useEffect(() => {
    document.title = activeTab;
  }, [activeTab]);

  return (
    <UserProvider>
      <Navigator setActiveTab={setActiveTab} />
    </UserProvider>
  );
}

export default App;