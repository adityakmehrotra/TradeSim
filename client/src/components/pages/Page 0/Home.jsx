import React, { useEffect, useState } from 'react';
import { Button, Container, Row, Col } from 'react-bootstrap';
import './Home.css';

const stockTickers = ['AAPL', 'TSLA', 'AMZN', 'GOOG', 'MSFT', 'SPY', 'NFLX', 'NVDA', 'META', 'JPM', 'DIS'];
const stockNames = ['Apple', 'Tesla', 'Amazon', 'Google', 'Microsoft', 'S&P 500', 'Netflix', 'NVIDIA', 'Meta', 'JP Morgan Chase', 'Disney'];

const backendURL = process.env.REACT_APP_BACKEND_URL;
const apiKey = process.env.REACT_APP_API_KEY;

function Home({ setActiveTab }) {
  const [stocksData, setStocksData] = useState([]);

  useEffect(() => {
    setActiveTab('Investing | TradeSim');
    fetchStockData();
  }, [setActiveTab]);

  const fetchStockData = async () => {
    const fetchedData = await Promise.all(stockTickers.map(ticker => fetchStockInfo(ticker)));
    setStocksData(fetchedData);
  };

  const fetchStockInfo = async (ticker) => {
    try {
      const response = await fetch(`https://api.polygon.io/v2/snapshot/locale/us/markets/stocks/tickers/${ticker}?apiKey=${apiKey}`);
      const data = await response.json();
      const index = stockTickers.indexOf(ticker);
      return {
        ticker: data.ticker.ticker,
        name: stockNames[index],
        price: data.ticker.day.c,
        change: data.ticker.todaysChange,
        changePerc: data.ticker.todaysChangePerc
      };
    } catch (error) {
      console.error("Error fetching stock data:", error);
      return null;
    }
  };

  return (
    <Container fluid className="home-container" style={{ minHeight: '100vh', padding: '50px 0', position: 'relative' }}>
      <Row className="text-center align-items-center" style={{ height: '100%' }}>
        <Col md={6} className="text-md-start">
          <h1 style={{ fontSize: '3.5rem', fontWeight: 'bold', color: '#343a40' }}>
            Welcome to <span style={{ color: '#007bff' }}>TradeSim</span>
          </h1>
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

      <Container
          fluid
          style={{ padding: '20px 0', position: 'absolute', bottom: 0, width: '100%', overflow: 'hidden' }}
      >
        <div className="scrolling-ticker reverse-scroll">
          {stocksData.length > 0 && stocksData.concat(stocksData).map((stock, index) => (
              <div className="stock-card" key={index} onClick={() => window.location.href = `/search/${stock.ticker}`}>
                <h5>{stock.ticker}</h5>
                <p>{stock.name}</p>
                <p>${stock.price.toFixed(2)}</p>
                {stock.change >= 0 ?
                    <p style={{ color: 'green' }}>
                      +{stock.change.toFixed(2)} (+{stock.changePerc.toFixed(2)}%)
                    </p> :
                    <p style={{ color: 'red' }}>
                      {stock.change.toFixed(2)} ({stock.changePerc.toFixed(2)}%)
                    </p>
                }
              </div>
          ))}
        </div>
      </Container>
    </Container>
  );
}

export default Home;
