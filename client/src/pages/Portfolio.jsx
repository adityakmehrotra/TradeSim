import { useState, useEffect, useCallback } from 'react';
import { Link, useParams } from 'react-router-dom';
import api from '../services/api';
import { priceFromCents, usd, signedUsd, signedPercent, shares } from '../lib/format';
import './Portfolio.css';

function Portfolio() {
  const { id } = useParams();
  const [portfolio, setPortfolio] = useState(null);
  const [positions, setPositions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const load = useCallback(async () => {
    try {
      const [nextPortfolio, nextPositions] = await Promise.all([
        api.getPortfolio(id),
        api.getPositions(id),
      ]);
      setPortfolio(nextPortfolio);
      setPositions(nextPositions);
      setError(null);
    } catch {
      setError('Failed to load this portfolio.');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    load();
    const interval = setInterval(load, 3000);
    return () => clearInterval(interval);
  }, [load]);

  if (loading) {
    return (
      <div className="portfolio-page">
        <div className="loading-spinner">Loading portfolio...</div>
      </div>
    );
  }

  if (error || !portfolio) {
    return (
      <div className="portfolio-page">
        <div className="error-message">{error || 'Portfolio not found'}</div>
        <Link to="/portfolios" className="btn btn-primary mt-4">
          Back to Portfolios
        </Link>
      </div>
    );
  }

  const cash = portfolio.cash || 0;
  const positionsValue = positions.reduce((total, position) => total + position.marketValue, 0);
  const totalValue = cash + positionsValue;
  const gainLoss = totalValue - (portfolio.initialBalance || 0);
  const gainLossPercent = portfolio.initialBalance
    ? (gainLoss / portfolio.initialBalance) * 100
    : 0;

  return (
    <div className="portfolio-page">
      <div className="portfolio-header">
        <div className="portfolio-title">
          <Link to="/portfolios" className="back-link">
            ← All Portfolios
          </Link>
          <h1>{portfolio.name}</h1>
        </div>
      </div>

      <div className="portfolio-summary-grid">
        <div className="summary-card">
          <div className="summary-card-title">Total Value</div>
          <div className="summary-card-value">{usd(totalValue)}</div>
          <div className={`summary-card-change ${gainLoss >= 0 ? 'positive' : 'negative'}`}>
            {signedUsd(gainLoss)} ({signedPercent(gainLossPercent)})
          </div>
        </div>
        <div className="summary-card">
          <div className="summary-card-title">Cash</div>
          <div className="summary-card-value">{usd(cash)}</div>
        </div>
        <div className="summary-card">
          <div className="summary-card-title">Invested</div>
          <div className="summary-card-value">{usd(positionsValue)}</div>
        </div>
        <div className="summary-card">
          <div className="summary-card-title">Initial Balance</div>
          <div className="summary-card-value">{usd(portfolio.initialBalance || 0)}</div>
        </div>
      </div>

      <div className="table-container">
        {positions.length === 0 ? (
          <div className="empty-state">
            <p>No positions yet. Find a symbol and place your first order.</p>
            <Link to="/" className="btn btn-primary">
              Browse the market
            </Link>
          </div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Symbol</th>
                <th>Shares</th>
                <th>Last</th>
                <th>Value</th>
                <th>Unrealized</th>
                <th>Realized</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {positions.map((position) => (
                <tr key={position.symbol}>
                  <td className="symbol-cell">{position.symbol}</td>
                  <td>{shares(position.quantity)}</td>
                  <td>${priceFromCents(position.lastCents)}</td>
                  <td>{usd(position.marketValue)}</td>
                  <td className={position.unrealizedPnl >= 0 ? 'positive' : 'negative'}>
                    {signedUsd(position.unrealizedPnl)}
                  </td>
                  <td className={position.realizedPnl >= 0 ? 'positive' : 'negative'}>
                    {signedUsd(position.realizedPnl)}
                  </td>
                  <td>
                    <Link to={`/stock/${position.symbol}`} className="btn btn-primary">
                      Trade
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

export default Portfolio;
