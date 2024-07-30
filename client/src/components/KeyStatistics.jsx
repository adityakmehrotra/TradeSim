import React from 'react';
import { Card, Row, Col } from 'react-bootstrap';

function KeyStatistics({ stats, ticker }) {
  if (!stats) {
    return null;
  }

  const cardStyle = {
    border: 'none',
    boxShadow: 'none'
  };

  return (
    <Col>
      <Card style={cardStyle}>
        <Card.Body>
          <Row>
            <Col className="text-center">
              <div><strong>Open</strong></div>
              <div>${stats.o.toFixed(2)}</div>
            </Col>
            <Col className="text-center">
              <div><strong>Low</strong></div>
              <div>${stats.l.toFixed(2)}</div>
            </Col>
            <Col className="text-center">
              <div><strong>High</strong></div>
              <div>${stats.h.toFixed(2)}</div>
            </Col>
            <Col className="text-center">
              <div><strong>Close</strong></div>
              <div>${stats.c.toFixed(2)}</div>
            </Col>
            <Col className="text-center">
              <div><strong>Volume</strong></div>
              <div>{formatVolume(stats.v)}</div>
            </Col>
            <Col className="text-center">
              <div><strong>Volume Weighted Avg</strong></div>
              <div>${stats.vw.toFixed(2)}</div>
            </Col>
          </Row>
        </Card.Body>
      </Card>
    </Col>
  );
}

function formatVolume(volume) {
  if (volume >= 1000000) {
    return `${(volume / 1000000).toFixed(1)}M`;
  } else if (volume >= 1000) {
    return `${(volume / 1000).toFixed(1)}K`;
  }
  return volume.toString();
}

export default KeyStatistics;