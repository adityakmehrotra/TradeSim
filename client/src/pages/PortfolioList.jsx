import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useSession } from '../context/SessionContext';
import CreatePortfolioModal from '../components/portfolio/CreatePortfolioModal';
import api from '../services/api';
import { usdFromCents } from '../lib/format';
import './PortfolioList.css';

function PortfolioList() {
  const { accountId, portfolios, loading, error, refresh } = useSession();
  const [isCreating, setIsCreating] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [createError, setCreateError] = useState(null);
  const navigate = useNavigate();

  const handleCreatePortfolio = async (formData) => {
    if (isCreating) return;

    try {
      setIsCreating(true);
      setCreateError(null);

      const { id } = await api.createPortfolio({
        accountID: accountId,
        name: formData.portfolioName,
        description: 'Paper trading portfolio',
        cashCents: Math.round(formData.initialBalance * 100),
        initialBalanceCents: Math.round(formData.initialBalance * 100),
      });

      await refresh();
      setShowModal(false);
      navigate(`/portfolio/${id}`);
    } catch (err) {
      console.error('Error creating portfolio:', err);
      setCreateError('Failed to create a new portfolio. Please try again.');
    } finally {
      setIsCreating(false);
    }
  };

  if (loading) {
    return (
      <div className="portfolio-list-page">
        <div className="loading-spinner">Loading your portfolios...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="portfolio-list-page">
        <div className="error-message">{error}</div>
        <button className="btn btn-primary mt-4" onClick={() => window.location.reload()}>
          Try Again
        </button>
      </div>
    );
  }

  return (
    <div className="portfolio-list-page">
      <div className="portfolio-list-header">
        <h1>Your Portfolios</h1>
        <button
          className="btn btn-primary"
          onClick={() => setShowModal(true)}
          disabled={isCreating}
        >
          {isCreating ? (
            <>
              <span className="btn-spinner"></span>
              Creating...
            </>
          ) : (
            <>
              <span className="btn-icon">+</span> Create Portfolio
            </>
          )}
        </button>
      </div>

      {createError && <div className="error-message">{createError}</div>}

      {portfolios.length === 0 ? (
        <div className="empty-portfolios">
          <h2>No Portfolios Yet</h2>
          <p>You don't have any portfolios yet. Create one to start trading.</p>
          <button
            className="btn btn-primary mt-4"
            onClick={() => setShowModal(true)}
            disabled={isCreating}
          >
            {isCreating ? 'Creating...' : 'Create Your First Portfolio'}
          </button>
        </div>
      ) : (
        <div className="portfolio-grid">
          {portfolios.map((portfolio) => {
            const cashCents = portfolio.cashCents || 0;
            const initialBalanceCents = portfolio.initialBalanceCents || 0;

            return (
              <Link
                to={`/portfolio/${portfolio.portfolioID}`}
                className="portfolio-card"
                key={portfolio.portfolioID}
              >
                <div className="portfolio-card-header">
                  <h3>{portfolio.name}</h3>
                  <span className="portfolio-id">#{portfolio.portfolioID}</span>
                </div>

                <div className="portfolio-card-body">
                  <div className="portfolio-cash">
                    <span className="label">Cash</span>
                    <span className="value">{usdFromCents(cashCents)}</span>
                  </div>

                  {/* Cash alone is not a return: buying shares moves cash into positions. Total
                      equity needs the position values, which this list does not load yet. */}
                  <div className="portfolio-return">
                    <span className="label">Started With</span>
                    <span className="value">{usdFromCents(initialBalanceCents)}</span>
                  </div>
                </div>

                <div className="portfolio-card-footer">
                  <span className="view-details">View Details</span>
                  <span className="arrow-icon">→</span>
                </div>
              </Link>
            );
          })}
        </div>
      )}

      <CreatePortfolioModal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        onSubmit={handleCreatePortfolio}
        isCreating={isCreating}
      />
    </div>
  );
}

export default PortfolioList;
