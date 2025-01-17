import React, { useState, useEffect } from 'react';
import './App.css';
import Navigator from './components/Navigator';
import { UserProvider } from './UserContext';
import { Modal, Button } from 'react-bootstrap';

function App() {
  const [activeTab, setActiveTab] = useState("TradeSim");
  const [maintenanceModalVisible, setMaintenanceModalVisible] = useState(true);
  const maintenanceEndTime = new Date("2025-01-19T06:00:00Z"); // UTC time for Jan 19, 2025, 12:00 AM CST

  useEffect(() => {
    document.title = activeTab;
  }, [activeTab]);

  useEffect(() => {
    const now = new Date();
    if (now >= maintenanceEndTime) {
      setMaintenanceModalVisible(false);
    }
  }, []);

  const closeMaintenanceModal = () => {
    const now = new Date();
    if (now >= maintenanceEndTime) {
      setMaintenanceModalVisible(false);
    } else {
      alert("The website is under maintenance until January 19, 2025, 12:00 AM CST.");
    }
  };

  return (
    <UserProvider>
      <Modal show={maintenanceModalVisible} backdrop="static" keyboard={false}>
        <Modal.Header>
          <Modal.Title>Maintenance in Progress</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          Our website is currently undergoing maintenance. Services will resume on January 19, 2025, at 12:00 AM CST. Thank you for your patience.
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
