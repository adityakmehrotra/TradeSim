import React, { useEffect } from 'react';
import { Button, Container, Row, Col } from 'react-bootstrap';

function Home({ setActiveTab }) {
  useEffect(() => {
    setActiveTab('Investing | TradeSim');
  }, [setActiveTab]);

  return (
    <Container fluid className="home-container" style={{ minHeight: '100vh', padding: '50px 0' }}>
      <Row className="text-center align-items-center" style={{ height: '100%' }}>
        <Col md={6} className="text-md-start">
          <h1 style={{ fontSize: '3.5rem', fontWeight: 'bold', color: '#343a40' }}>Welcome to <span style={{ color: '#007bff' }}>TradeSim</span></h1>
          <p style={{ fontSize: '1.5rem', color: '#6c757d', marginTop: '20px' }}>
            Your gateway to mastering stock market trading in a risk-free environment.
          </p>
          <Button
            href="/portfolio"
            variant="primary"
            style={{ padding: '10px 25px', fontSize: '1.25rem', marginTop: '30px' }}
          >
            Get Started
          </Button>
        </Col>
      </Row>
    </Container>
  );
}

export default Home;
