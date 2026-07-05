import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useSession } from '../../context/SessionContext';
import './Account.css';

function Account() {
  const { accountId, portfolios, loading, reset } = useSession();
  const [isResetting, setIsResetting] = useState(false);

  const handleReset = async () => {
    if (isResetting) return;
    const confirmed = window.confirm(
      'Reset your account? This clears every portfolio and starts you over with fresh virtual cash.'
    );
    if (!confirmed) return;

    try {
      setIsResetting(true);
      await reset();
    } finally {
      setIsResetting(false);
    }
  };

  if (loading) {
    return (
      <div className="account-container">
        <div className="account-loading">
          <div className="spinner"></div>
          <p>Loading your account...</p>
        </div>
      </div>
    );
  }

  const totalCash = portfolios.reduce((sum, portfolio) => sum + (portfolio.cash || 0), 0);

  return (
    <div className="account-container">
      <div className="account-header">
        <div className="account-header-info">
          <h1>Account</h1>
          <p>This account is tied to your browser. There is no login.</p>
        </div>
        <button className="btn-logout" onClick={handleReset} disabled={isResetting}>
          {isResetting ? 'Resetting...' : 'Reset Account'}
        </button>
      </div>

      <div className="account-content">
        <div className="account-section">
          <div className="profile-details-grid">
            <div className="detail-item">
              <div className="detail-label">Account ID</div>
              <div className="detail-value">{accountId ?? '-'}</div>
            </div>
            <div className="detail-item">
              <div className="detail-label">Portfolios</div>
              <div className="detail-value">{portfolios.length}</div>
            </div>
            <div className="detail-item">
              <div className="detail-label">Total Cash</div>
              <div className="detail-value">${totalCash.toFixed(2)}</div>
            </div>
          </div>
        </div>

        <div className="account-section portfolios-section">
          <div className="section-header">
            <h2>Your Portfolios</h2>
            <Link to="/portfolios" className="btn-secondary">
              View All
            </Link>
          </div>

          {portfolios.length === 0 ? (
            <div className="empty-portfolios">
              <p>You have no portfolios yet.</p>
              <Link to="/portfolios" className="btn-primary">
                Create One
              </Link>
            </div>
          ) : (
            <div className="portfolio-list">
              {portfolios.map((portfolio) => (
                <div key={portfolio.portfolioID} className="portfolio-item">
                  <div className="portfolio-info">
                    <h3>{portfolio.name}</h3>
                    <div className="portfolio-balance">${(portfolio.cash || 0).toFixed(2)}</div>
                  </div>
                  <Link to={`/portfolio/${portfolio.portfolioID}`} className="btn-text">
                    View Details →
                  </Link>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default Account;
