import { useEffect, useState } from "react";
import { Card, Container, Row, Col } from "react-bootstrap";
import { useParams } from "react-router-dom";
import { Pie } from 'react-chartjs-2';
import 'bootstrap/dist/css/bootstrap.min.css';
import { Chart, ArcElement, Tooltip, Legend } from 'chart.js';

Chart.register(ArcElement, Tooltip, Legend);

export default function PortfolioDetails() {
  const { portfolioID } = useParams();
  const [portfolio, setPortfolio] = useState(null);
  const [assetPrices, setAssetPrices] = useState({});
  const [chartData, setChartData] = useState(null);

  useEffect(() => {
    fetch(`http://localhost:8000/paper_trader/portfolio/get?id=${portfolioID}`)
      .then(response => response.json())
      .then(data => {
        setPortfolio(data);
        if (data.assets) {
          fetchAssetPrices(data.assets);
        }
      })
      .catch(error => console.error('Error fetching portfolio details:', error));
  }, [portfolioID]);

  const fetchAssetPrices = (assets) => {
    const assetCodes = Object.keys(assets);
    const prices = {};

    Promise.all(
      assetCodes.map(code => 
        fetch(`http://localhost:8000/paper_trader/security/get/price?ticker=${code}`)
          .then(response => response.json())
          .then(price => {
            prices[code] = price;
          })
          .catch(error => console.error(`Error fetching price for ${code}:`, error))
      )
    ).then(() => {
      setAssetPrices(prices);
    });
  };

  useEffect(() => {
    if (portfolio && Object.keys(assetPrices).length > 0) {
      const data = {
        labels: [...Object.keys(portfolio.assets), 'Cash'],
        datasets: [
          {
            label: 'Portfolio Distribution',
            data: [
              ...Object.keys(portfolio.assets).map(code => 
                portfolio.assets[code].sharesOwned * assetPrices[code]
              ),
              portfolio.cashAmount
            ],
            backgroundColor: [
              '#FF6384',
              '#36A2EB',
              '#FFCE56',
              '#FF6384',
              '#36A2EB'
            ],
            hoverBackgroundColor: [
              '#FF6384',
              '#36A2EB',
              '#FFCE56',
              '#FF6384',
              '#36A2EB'
            ]
          }
        ]
      };
      setChartData(data);
    }
  }, [portfolio, assetPrices]);

  if (!portfolio) {
    return <p>Loading...</p>;
  }

  return (
    <Container>
      <Row className="mb-4">
        <Col>
          <h2>{portfolio.portfolioName}</h2>
          <p>Initial Investment: ${portfolio.cashAmount.toFixed(2)}</p>
        </Col>
      </Row>
      {chartData && (
        <Row>
          <Col>
            <Card>
              <Card.Body>
                <Pie data={chartData} />
              </Card.Body>
            </Card>
          </Col>
        </Row>
      )}
      {/* Add more details about the portfolio here */}
    </Container>
  );
}
