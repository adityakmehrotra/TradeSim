import React, { useState, useEffect } from 'react';
import './App.css';
import Navigator from './components/Navigator';
import { UserProvider } from './UserContext';
import { Modal, Button } from 'react-bootstrap';

function App() {
  const [activeTab, setActiveTab] = useState("TradeSim");
  const [maintenanceModalVisible, setMaintenanceModalVisible] = useState(true);
  const [timeRemaining, setTimeRemaining] = useState("");
  const maintenanceEndTime = new Date("2025-01-20T06:00:00Z"); // UTC time for Jan 20, 2025, 12:00 AM CST

  useEffect(() => {
    document.title = activeTab;
  }, [activeTab]);

  useEffect(() => {
    const now = new Date();
    if (now >= maintenanceEndTime) {
      setMaintenanceModalVisible(false);
    } else {
      const interval = setInterval(() => {
        const currentTime = new Date();
        const difference = maintenanceEndTime - currentTime;
        if (difference <= 0) {
          setMaintenanceModalVisible(false);
          clearInterval(interval);
        } else {
          const hours = Math.floor((difference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
          const minutes = Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60));
          const seconds = Math.floor((difference % (1000 * 60)) / 1000);
          setTimeRemaining(`${hours}h ${minutes}m ${seconds}s`);
        }
      }, 1000);

      return () => clearInterval(interval);
    }
  }, []);

  const closeMaintenanceModal = () => {
    const now = new Date();
    if (now >= maintenanceEndTime) {
      setMaintenanceModalVisible(false);
    } else {
      alert("The website is under maintenance until January 20, 2025, 12:00 AM CST.");
    }
  };

  return (
    <UserProvider>
      <Modal show={maintenanceModalVisible} backdrop="static" keyboard={false}>
        <Modal.Header>
          <Modal.Title>Maintenance in Progress</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          Our website is currently undergoing maintenance. Services will resume on January 20, 2025, at 12:00 AM CST. Thank you for your patience.
          {timeRemaining && (
            <div style={{ marginTop: "10px", fontWeight: "bold" }}>
              Time remaining: {timeRemaining}
            </div>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="primary" onClick={closeMaintenanceModal} disabled={new Date() < maintenanceEndTime}>
            OK
          </Button>
        </Modal.Footer>
      </Modal>
      {!maintenanceModalVisible && <Navigator setActiveTab={setActiveTab} />}
    </UserProvider>
  );
}

export default App;
