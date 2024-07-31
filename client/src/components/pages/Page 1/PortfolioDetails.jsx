import { useEffect, useState } from "react";
import { Card, Container, Row, Col } from "react-bootstrap";
import { useParams } from "react-router-dom";
import 'bootstrap/dist/css/bootstrap.min.css';

export default function PortfolioDetails() {
  const { portfolioID } = useParams();
  const [portfolio, setPortfolio] = useState(null);

  useEffect(() => {
    fetch(`http://localhost:8000/paper_trader/portfolio/get/portfolio?portfolioID=${portfolioID}`)
      .then(response => response.json())
      .then(data => setPortfolio(data))
      .catch(error => console.error('Error fetching portfolio details:', error));
  }, [portfolioID]);

  if (!portfolio) {
    return <p>Loading...</p>;
  }

  return (
    <Container>
      <Row className="mb-4">
        <Col>
          <h2>{portfolio.portfolioName}</h2>
          <p>Cash: ${portfolio.cashAmount.toFixed(2)}</p>
        </Col>
      </Row>
      {/* Add more details about the portfolio here */}
    </Container>
  );
}
