// Home.js
import React, { useEffect } from 'react';

function Home({ setActiveTab }) {
  useEffect(() => {
    setActiveTab('Home');
  }, [setActiveTab]);

  return (
    <div>
      <h1>Welcome to TradeSim</h1>
      <p>Your gateway to simulated stock market trading.</p>
    </div>
  );
}

export default Home;
