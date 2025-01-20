import React, { useState, useEffect } from 'react';
import './App.css';
import Navigator from './components/Navigator';
import { UserProvider } from './UserContext';
import { Modal, Button } from 'react-bootstrap';

function App() {
  const [activeTab, setActiveTab] = useState("TradeSim");
  const [showMaintenance, setShowMaintenance] = useState(false);
  const [timeRemaining, setTimeRemaining] = useState("");

  useEffect(() => {
    document.title = activeTab;

    const maintenanceEnd = new Date('2025-01-21T06:00:00Z');

    const updateTimer = () => {
      const currentTime = new Date();
      const difference = maintenanceEnd - currentTime;

      if (difference > 0) {
        setShowMaintenance(true);
        const hours = Math.floor(difference / (1000 * 60 * 60));
        const minutes = Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((difference % (1000 * 60)) / 1000);
        setTimeRemaining(`${hours} hours, ${minutes} minutes, ${seconds} seconds`);
      } else {
        setShowMaintenance(false);
      }
    };

    updateTimer();
    const interval = setInterval(updateTimer, 1000);

    return () => clearInterval(interval);
  }, [activeTab]);

  return (
    <UserProvider>
      <Navigator setActiveTab={setActiveTab} />

      <Modal show={showMaintenance} backdrop="static" keyboard={false} centered>
        <Modal.Header>
          <Modal.Title>Maintenance in Progress</Modal.Title>
        </Modal.Header>
        <Modal.Body className="text-center">
          TradeSim is currently undergoing maintenance. Services will resume on January 21, 2025, at 12:00 AM CST.
          <br />
          <br />
          <strong>{timeRemaining}</strong>
          <br />
          <br />
          Thank you for your patience!
        </Modal.Body>
        <Modal.Footer>
          <Button variant="primary" disabled>OK</Button>
        </Modal.Footer>
      </Modal>
    </UserProvider>
  );
}

export default App;
