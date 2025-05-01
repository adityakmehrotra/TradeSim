import { useState, useEffect, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import CreatePortfolioModal from '../components/portfolio/CreatePortfolioModal';
import api from '../services/api';
import './PortfolioList.css';

function PortfolioList() {
  const { user } = useContext(AuthContext);
  const [portfolios, setPortfolios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isCreating, setIsCreating] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }

    const fetchPortfolios = async () => {
      try {
        setLoading(true);
        
        const allPortfolios = await api.getAllPortfolios();
        
        const userPortfolios = allPortfolios.filter(
          portfolio => portfolio.accountID === user.accountId
        );
        
        setPortfolios(userPortfolios);
      } catch (err) {
        console.error('Error fetching portfolios:', err);
        setError('Failed to load your portfolios. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    fetchPortfolios();
  }, [user, navigate]);

  const handleCreatePortfolio = async (formData) => {
    if (isCreating) return;
    
    try {
      setIsCreating(true);
      
      const nextId = await api.getNextPortfolioId();
      
      const newPortfolio = {
        portfolioID: nextId,
        accountID: user.accountId,
        portfolioName: formData.portfolioName,
        cashAmount: formData.initialBalance,
        initialBalance: formData.initialBalance,
        transactionList: [],
        assets: {
          "Cash": {
            sharesOwned: 1.0,
            initialCashInvestment: formData.initialBalance,
            gmtTime: new Date().toISOString(),
            initialPrice: formData.initialBalance
          }
        },
        holdings: [],
        assetsAvgValue: {
          "Cash": formData.initialBalance
        }
      };
      
      const createdPortfolio = await api.createPortfolio(newPortfolio);
      
      setPortfolios(prevPortfolios => [...prevPortfolios, createdPortfolio]);
      
      setShowModal(false);
      
      navigate(`/portfolio/${createdPortfolio.portfolioID}`);
      
    } catch (err) {
      console.error('Error creating portfolio:', err);
      setError('Failed to create a new portfolio. Please try again.');
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
      
      {portfolios.length === 0 ? (
        <div className="empty-portfolios">
          <div className="empty-icon">📊</div>
          <h2>No Portfolios Yet</h2>
          <p>You don't have any portfolios yet. Create one to start investing!</p>
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
          {portfolios.map(portfolio => {
            const assetsValue = Object.values(portfolio.assets || {}).reduce((total, asset) => {
              return total + (asset.sharesOwned * asset.initialPrice || 0);
            }, 0);
            
            const totalValue = (portfolio.cashAmount || 0) + assetsValue;
            const gainLoss = totalValue - (portfolio.initialBalance || 0);
            const gainLossPercent = portfolio.initialBalance ? (gainLoss / portfolio.initialBalance) * 100 : 0;
            
            return (
              <Link 
                to={`/portfolio/${portfolio.portfolioID}`} 
                className="portfolio-card" 
                key={portfolio.portfolioID}
              >
                <div className="portfolio-card-header">
                  <h3>{portfolio.portfolioName}</h3>
                  <span className="portfolio-id">#{portfolio.portfolioID}</span>
                </div>
                
                <div className="portfolio-card-body">
                  <div className="portfolio-value">
                    <span className="label">Total Value</span>
                    <span className="value">${totalValue.toFixed(2)}</span>
                  </div>
                  
                  <div className="portfolio-cash">
                    <span className="label">Cash</span>
                    <span className="value">${portfolio.cashAmount.toFixed(2)}</span>
                  </div>
                  
                  <div className="portfolio-return">
                    <span className="label">Return</span>
                    <span className={`value ${gainLoss >= 0 ? 'positive' : 'negative'}`}>
                      {gainLoss >= 0 ? '+' : ''}{gainLoss.toFixed(2)} ({gainLossPercent.toFixed(2)}%)
                    </span>
                  </div>
                  
                  <div className="portfolio-holdings-count">
                    <span className="label">Holdings</span>
                    <span className="value">{Object.keys(portfolio.assets || {}).length - 1}</span> {/* Subtract 1 for cash */}
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