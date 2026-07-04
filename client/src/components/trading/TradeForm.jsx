import { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../../context/AuthContext';
import CreatePortfolioModal from '../portfolio/CreatePortfolioModal';
import api from '../../services/api';
import './TradeForm.css';

function TradeForm({ symbol, currentPrice }) {
  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5001';

  const { user } = useContext(AuthContext);
  const [orderType, setOrderType] = useState('buy');
  const [shares, setShares] = useState(1);
  const [orderSubmitted, setOrderSubmitted] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [selectedPortfolio, setSelectedPortfolio] = useState(null);
  const [portfolios, setPortfolios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [cashLoading, setCashLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [isCreating, setIsCreating] = useState(false);
  const [availableCash, setAvailableCash] = useState(0);
  
  useEffect(() => {
    const fetchPortfolios = async () => {
      if (!user) {
        setLoading(false);
        return;
      }
      
      try {
        setLoading(true);
        
        const allPortfolios = await api.getAllPortfolios();
        
        const userPortfolios = allPortfolios.filter(
          portfolio => portfolio.accountID === user.accountId
        );
        
        const formattedPortfolios = userPortfolios.map(portfolio => ({
          id: portfolio.portfolioID,
          name: portfolio.portfolioName,
          holdings: portfolio.assets ? Object.entries(portfolio.assets)
            .map(([symbol, asset]) => ({
              symbol: symbol,
              shares: asset.sharesOwned
            }))
            .filter(asset => asset.symbol !== 'Cash') : []
        }));
        
        setPortfolios(formattedPortfolios);
        
        if (formattedPortfolios.length > 0) {
          const firstPortfolioId = formattedPortfolios[0].id;
          setSelectedPortfolio(firstPortfolioId);
          fetchPortfolioCash(firstPortfolioId);
        }
        
        setError(null);
      } catch (err) {
        console.error("Error fetching portfolios:", err);
        setError("Failed to load your portfolios");
      } finally {
        setLoading(false);
      }
    };
    
    fetchPortfolios();
  }, [user]);
  
  const fetchPortfolioCash = async (portfolioId) => {
    if (!portfolioId) return;
    
    try {
      setCashLoading(true);
      
      const response = await fetch(`${API_BASE_URL}/paper_trader/portfolio/get/cash?id=${portfolioId}`);
      
      if (!response.ok) {
        throw new Error(`Failed to fetch cash: ${response.status}`);
      }
      
      const data = await response.json();
      console.log(response);
      console.log("Fetched cash data:", data);
      setAvailableCash(data || 0);
      
    } catch (err) {
      console.error("Error fetching portfolio cash:", err);
    } finally {
      setCashLoading(false);
    }
  };
  
  const handlePortfolioChange = (e) => {
    const newPortfolioId = e.target.value;
    setSelectedPortfolio(newPortfolioId);
    fetchPortfolioCash(newPortfolioId);
  };
  
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
      
      const formattedNewPortfolio = {
        id: createdPortfolio.portfolioID,
        name: createdPortfolio.portfolioName,
        holdings: []
      };
      
      setPortfolios(prevPortfolios => [...prevPortfolios, formattedNewPortfolio]);
      setSelectedPortfolio(formattedNewPortfolio.id);
      setAvailableCash(formData.initialBalance);
      
      setShowModal(false);
      
    } catch (err) {
      console.error('Error creating portfolio:', err);
      setError('Failed to create a new portfolio. Please try again.');
    } finally {
      setIsCreating(false);
    }
  };
  
  const getCurrentPortfolio = () => {
    if (!selectedPortfolio || !portfolios.length) return null;
    return portfolios.find(p => p.id === selectedPortfolio);
  };
  
  const currentPortfolio = getCurrentPortfolio();
  const ownedShares = currentPortfolio 
    ? (currentPortfolio.holdings?.find(h => h.symbol === symbol)?.shares || 0) 
    : 0;
  
  const handleSharesChange = (e) => {
    const value = e.target.value === '' ? '' : Math.max(0, parseInt(e.target.value) || 0);
    setShares(value);
  };
  
  const calculateTotal = () => {
    return shares ? (shares * currentPrice).toFixed(2) : '0.00';
  };

  const isValid = () => {
    if (!selectedPortfolio) return false;
    if (!shares || shares <= 0) return false;
    if (orderType === 'buy' && shares * currentPrice > availableCash) return false;
    if (orderType === 'sell' && shares > ownedShares) return false;
    return true;
  };
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!isValid()) return;
    
    setIsSubmitting(true);
    
    try {
      const transaction = {
        securityCode: symbol,
        orderType: orderType === 'buy' ? 'Buy' : 'Sell',
        shareAmount: shares,
        cashAmount: shares * currentPrice,
        currPrice: currentPrice,
        gmtTime: new Date().toISOString()
      };
      
      await api.executeTrade(selectedPortfolio, transaction);
      
      fetchPortfolioCash(selectedPortfolio);
      
      setOrderSubmitted(true);
      
      setTimeout(() => {
        setOrderSubmitted(false);
        setShares(1);
      }, 3000);
    } catch (error) {
      console.error("Trade execution failed:", error);
      setError("Failed to execute trade. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  };
  
  if (loading) {
    return (
      <div className="trade-form">
        <h3>Place Order</h3>
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading your portfolios...</p>
        </div>
      </div>
    );
  }
  
  if (orderSubmitted) {
    const portfolioName = currentPortfolio ? currentPortfolio.name : '';
    
    return (
      <div className="trade-form">
        <div className="order-confirmation">
          <div className="confirmation-icon">✓</div>
          <h3>Order Successful</h3>
          <p>
            You {orderType === 'buy' ? 'bought' : 'sold'} <span className="highlight">{shares} {shares === 1 ? 'share' : 'shares'}</span> of <span className="highlight">{symbol}</span>
          </p>
          <div className="confirmation-details">
            <div className="detail-row">
              <span>Portfolio</span>
              <span>{portfolioName}</span>
            </div>
            <div className="detail-row">
              <span>Price</span>
              <span>${currentPrice.toFixed(2)}</span>
            </div>
            <div className="detail-row total">
              <span>Total {orderType === 'buy' ? 'Cost' : 'Credit'}</span>
              <span>${calculateTotal()}</span>
            </div>
          </div>
        </div>
      </div>
    );
  }
  
  if (portfolios.length === 0) {
    return (
      <div className="trade-form">
        <h3>Place Order</h3>
        <div className="no-portfolio">
          <div className="no-portfolio-icon">📊</div>
          <h4>No Portfolios Available</h4>
          <p>You need a portfolio to start trading.</p>
          <button 
            className="create-portfolio-button"
            onClick={() => setShowModal(true)}
          >
            Create Portfolio
          </button>
        </div>
        
        <CreatePortfolioModal
          isOpen={showModal}
          onClose={() => setShowModal(false)}
          onSubmit={handleCreatePortfolio}
          isCreating={isCreating}
        />
      </div>
    );
  }

  return (
    <div className="trade-form">
      <h3>Place Order</h3>
      
      <div className="order-type-tabs">
        <button
          className={`tab-button ${orderType === 'buy' ? 'active buy-active' : ''}`}
          onClick={() => setOrderType('buy')}
        >
          Buy
        </button>
        <button
          className={`tab-button ${orderType === 'sell' ? 'active sell-active' : ''}`}
          onClick={() => setOrderType('sell')}
        >
          Sell
        </button>
      </div>
      
      <form onSubmit={handleSubmit}>
        <div className="form-field">
          <div className="field-header">
            <label>Symbol</label>
          </div>
          <div className="field-value symbol">{symbol}</div>
        </div>
        
        <div className="form-field">
          <div className="field-header">
            <label htmlFor="portfolio">Portfolio</label>
          </div>
          <div className="portfolio-selector-container">
            <select
              id="portfolio"
              value={selectedPortfolio || ''}
              onChange={handlePortfolioChange}
              className="portfolio-selector"
              disabled={cashLoading || isSubmitting}
            >
              {portfolios.map(portfolio => (
                <option key={portfolio.id} value={portfolio.id}>
                  {portfolio.name}
                </option>
              ))}
            </select>
            <div className="select-arrow">▼</div>
          </div>
        </div>
        
        <div className="form-field">
          <div className="field-header">
            <label>Market Price</label>
          </div>
          <div className="field-value price">${currentPrice.toFixed(2)}</div>
        </div>
        
        <div className="form-field">
          <div className="field-header">
            <label htmlFor="shares">Shares</label>
            {orderType === 'sell' && ownedShares > 0 && (
              <button 
                type="button" 
                className="max-button"
                onClick={() => setShares(ownedShares)}
              >
                Max
              </button>
            )}
          </div>
          <input
            type="number"
            id="shares"
            value={shares}
            onChange={handleSharesChange}
            min="1"
            step="1"
            className="shares-input"
          />
        </div>

        <div className="form-field">
          <div className="field-header">
            <label>Estimated {orderType === 'buy' ? 'Cost' : 'Credit'}</label>
          </div>
          <div className="field-value estimated-total">${calculateTotal()}</div>
        </div>

        {orderType === 'buy' && (
          <div className="form-field">
            <div className="field-header">
              <label>Available Cash</label>
              {cashLoading && <span className="mini-spinner"></span>}
            </div>
            {console.log('Available Cash:', availableCash)}
            <div className={`field-value cash-value ${availableCash < shares * currentPrice ? 'insufficient' : ''}`}>
              ${availableCash.toFixed(2)}
            </div>
          </div>
        )}

        {orderType === 'sell' && (
          <div className="form-field">
            <div className="field-header">
              <label>Available Shares</label>
            </div>
            <div className={`field-value shares-value ${ownedShares < shares ? 'insufficient' : ''}`}>
              {ownedShares}
            </div>
          </div>
        )}

        <div className="form-field portfolio-field">
          <div className="field-header">
            <label>Trading in</label>
          </div>
          <div className="field-value portfolio-name">
            {currentPortfolio?.name || 'Selected Portfolio'}
          </div>
        </div>

        <button 
          type="submit" 
          className={`trade-button ${orderType}-button`}
          disabled={!isValid() || isSubmitting || cashLoading}
        >
          {isSubmitting ? (
            <span className="button-spinner"></span>
          ) : (
            `${orderType === 'buy' ? 'Buy' : 'Sell'} ${shares || 0} ${shares === 1 ? 'Share' : 'Shares'}`
          )}
        </button>
        
        {orderType === 'buy' && shares * currentPrice > availableCash && (
          <div className="error-message">
            Insufficient funds for this transaction
          </div>
        )}
        
        {orderType === 'sell' && shares > ownedShares && (
          <div className="error-message">
            You don't own enough shares
          </div>
        )}
      </form>
      
      <CreatePortfolioModal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        onSubmit={handleCreatePortfolio}
        isCreating={isCreating}
      />
    </div>
  );
}

export default TradeForm;