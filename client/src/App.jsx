import React, { useState, useEffect } from 'react';
import './App.css';
import Navigator from './components/Navigator';
import { UserProvider } from './UserContext';
import { Modal, Button } from 'react-bootstrap';

function App() {
  const [activeTab, setActiveTab] = useState("TradeSim");
  const [showMaintenance, setShowMaintenance] = useState(false);

  useEffect(() => {
    document.title = activeTab;
    
    // Set maintenance end time (January 21, 2025, at 12:00 AM CST)
    const maintenanceEnd = new Date('2025-01-21T06:00:00Z');  // UTC time equivalent of CST 12:00 AM
    const currentTime = new Date();

    if (currentTime < maintenanceEnd) {
      setShowMaintenance(true);
    } else {
      setShowMaintenance(false);
    }
  }, [activeTab]);

  return (
    <UserProvider>
      <Navigator setActiveTab={setActiveTab} />

      {/* Maintenance Modal */}
      <Modal show={showMaintenance} backdrop="static" keyboard={false} centered>
        <Modal.Header>
          <Modal.Title>Maintenance in Progress</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          TradeSim is currently undergoing maintenance. Services will resume on January 21, 2025, at 12:00 AM CST. 
          Thank you for your patience.
        </Modal.Body>
        <Modal.Footer>
          <Button variant="primary" disabled>OK</Button>
        </Modal.Footer>
      </Modal>
    </UserProvider>
  );
}

export default App;
