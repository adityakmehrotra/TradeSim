import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';

function Home() {
  const [trendingStocks, setTrendingStocks] = useState([
    { symbol: 'AAPL', name: 'Apple Inc.', price: 187.54, change: 1.25, changePercent: 0.67 },
    { symbol: 'MSFT', name: 'Microsoft Corporation', price: 338.72, change: 2.45, changePercent: 0.73 },
    { symbol: 'AMZN', name: 'Amazon.com Inc.', price: 147.03, change: -1.02, changePercent: -0.69 },
    { symbol: 'GOOGL', name: 'Alphabet Inc.', price: 142.32, change: 0.87, changePercent: 0.62 },
    { symbol: 'TSLA', name: 'Tesla Inc.', price: 235.67, change: 5.23, changePercent: 2.27 }
  ]);

  return (
    <div>
      <div className="jumbotron bg-light p-5 mb-4 rounded">
        <h1 className="display-4">Welcome to TradeSim</h1>
        <p className="lead">Practice trading stocks without risking real money. Build your skills and test strategies in a realistic market environment.</p>
        <hr className="my-4" />
        <p>Start by creating a portfolio and making your first virtual trade today!</p>
        <Link to="/portfolio" className="btn btn-primary btn-lg">Get Started</Link>
      </div>

      <h2 className="mb-4">Trending Stocks</h2>
      <div className="row">
        {trendingStocks.map(stock => (
          <div key={stock.symbol} className="col-md-4 mb-4">
            <div className="card">
              <div className="card-body">
                <h5 className="card-title">
                  <Link to={`/stock/${stock.symbol}`}>{stock.symbol}</Link> - {stock.name}
                </h5>
                <h6 className="card-subtitle mb-2">${stock.price.toFixed(2)}</h6>
                <p className={`card-text ${stock.change >= 0 ? 'text-success' : 'text-danger'}`}>
                  {stock.change >= 0 ? '+' : ''}{stock.change.toFixed(2)} ({stock.change >= 0 ? '+' : ''}{stock.changePercent.toFixed(2)}%)
                </p>
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="ticker-container mt-5 p-2 bg-dark text-white overflow-hidden">
        <div className="ticker">
          {trendingStocks.map((stock, index) => (
            <span key={index} className="ticker-item me-4">
              {stock.symbol} <span className={stock.change >= 0 ? 'text-success' : 'text-danger'}>
                ${stock.price.toFixed(2)} {stock.change >= 0 ? '↑' : '↓'}
              </span>
            </span>
          ))}
        </div>
      </div>
    </div>
  );
}

export default Home;