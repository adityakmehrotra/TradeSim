import React from 'react';
import { Card, Row, Col } from 'react-bootstrap';

function AboutCompany({ company }) {
  if (!company) return null;

  const formatNumber = (num) => {
    return num.toLocaleString();
  };

  const formatMarketCap = (marketCap) => {
    if (marketCap >= 1e12) {
      return `${(marketCap / 1e12).toFixed(2)} Trillion`;
    } else if (marketCap >= 1e9) {
      return `${(marketCap / 1e9).toFixed(2)} Billion`;
    } else if (marketCap >= 1e6) {
      return `${(marketCap / 1e6).toFixed(2)} Million`;
    } else {
      return formatNumber(marketCap);
    }
  };

  return (
    <div style={{ marginBottom: '20px' }}>
      <Row>
        <Col>
          <h2 style={{ borderBottom: '2px solid #000' }}>About</h2>
        </Col>
      </Row>
      <Card style={{ border: 'none' }}>
        <Card.Body>
          <p><strong>Description:</strong> {company.description}</p>
          <Row className="text-center">
            <Col>
              <strong>Employees</strong> <div>{formatNumber(company.total_employees)}</div>
            </Col>
            <Col>
              <strong>Location</strong> <div>{company.address.city}, {company.address.state}</div>
            </Col>
            <Col>
              <strong>Market Cap</strong> <div>{formatMarketCap(company.market_cap)}</div>
            </Col>
          </Row>
        </Card.Body>
      </Card>
    </div>
  );
}

export default AboutCompany;