import { useState, useEffect, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../../context/AuthContext';
import './Account.css';

function Account() {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const [accountData, setAccountData] = useState(null);
  const [userPortfolios, setUserPortfolios] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5001';
  
  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }
    
    const fetchAccountData = async () => {
      setIsLoading(true);
      setError(null);
      
      try {
        const accountResponse = await fetch(`${API_BASE_URL}/paper_trader/account/get?id=${user.accountId}`);
        
        if (!accountResponse.ok) {
          throw new Error('Failed to fetch account data');
        }
        
        const accountData = await accountResponse.json();
        setAccountData(accountData);
        
        const portfoliosResponse = await fetch(`${API_BASE_URL}/paper_trader/portfolio/all`);
        
        if (!portfoliosResponse.ok) {
          throw new Error('Failed to fetch portfolios');
        }
        
        const allPortfolios = await portfoliosResponse.json();
        
        const userPortfolios = allPortfolios.filter(
          portfolio => portfolio.accountID === user.accountId
        );
        
        setUserPortfolios(userPortfolios);
      } catch (err) {
        console.error('Error fetching account data:', err);
        setError('Failed to load your account information. Please try again later.');
      } finally {
        setIsLoading(false);
      }
    };
    
    fetchAccountData();
  }, [user, navigate, API_BASE_URL]);
  
  const handleLogout = () => {
    logout();
    navigate('/login');
  };
  
  if (isLoading) {
    return (
      <div className="account-container">
        <div className="account-loading">
          <div className="spinner"></div>
          <p>Loading your account information...</p>
        </div>
      </div>
    );
  }
  
  if (error) {
    return (
      <div className="account-container">
        <div className="account-error">
          <div className="error-icon">!</div>
          <h2>Something went wrong</h2>
          <p>{error}</p>
          <button 
            className="btn-primary"
            onClick={() => window.location.reload()}
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  const calculateTotalValue = () => {
    if (!userPortfolios || userPortfolios.length === 0) return 0;
    
    return userPortfolios.reduce((total, portfolio) => {
      const portfolioValue = portfolio.cashAmount + 
        (Object.entries(portfolio.assets || {})
          .filter(([symbol]) => symbol !== 'Cash')
          .reduce((sum, [_, asset]) => sum + (asset.sharesOwned * asset.initialPrice), 0));
          
      return total + portfolioValue;
    }, 0);
  };

  return (
    <div className="account-container">
      <div className="account-header">
        <div className="account-header-info">
          <h1>Account Settings</h1>
          <p>Manage your profile and preferences</p>
        </div>
        <button 
          className="btn-logout" 
          onClick={handleLogout}
        >
          Sign Out
        </button>
      </div>
      
      <div className="account-content">
        <div className="account-section profile-section">
          <h2>Profile Information</h2>
          <div className="profile-card">
            <div className="profile-avatar">
              {accountData?.firstName?.charAt(0)}{accountData?.lastName?.charAt(0)}
            </div>
            <div className="profile-details">
              <div className="profile-name">
                {accountData?.firstName} {accountData?.lastName}
              </div>
              <div className="profile-username">@{user?.username}</div>
              <div className="profile-email">{accountData?.emailID}</div>
            </div>
          </div>
          
          <div className="profile-details-grid">
            <div className="detail-item">
              <div className="detail-label">Account ID</div>
              <div className="detail-value">{accountData?.accountID}</div>
            </div>
            <div className="detail-item">
              <div className="detail-label">Phone</div>
              <div className="detail-value">
                {accountData?.phoneNum || 'Not provided'}
              </div>
            </div>
            <div className="detail-item">
              <div className="detail-label">Member Since</div>
              <div className="detail-value">
                {new Date(accountData?.startDate).toLocaleDateString()}
              </div>
            </div>
            <div className="detail-item">
              <div className="detail-label">Status</div>
              <div className="detail-value status-active">Active</div>
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
          
          {userPortfolios.length === 0 ? (
            <div className="empty-portfolios">
              <p>You haven't created any portfolios yet.</p>
              <Link to="/portfolios" className="btn-primary">
                Create Your First Portfolio
              </Link>
            </div>
          ) : (
            <>
              <div className="account-stats">
                <div className="stat-card">
                  <div className="stat-value">${calculateTotalValue().toFixed(2)}</div>
                  <div className="stat-label">Total Portfolio Value</div>
                </div>
                <div className="stat-card">
                  <div className="stat-value">{userPortfolios.length}</div>
                  <div className="stat-label">Active Portfolios</div>
                </div>
              </div>
            
              <div className="portfolio-list">
                {userPortfolios.slice(0, 3).map(portfolio => (
                  <div key={portfolio.portfolioID} className="portfolio-item">
                    <div className="portfolio-info">
                      <h3>{portfolio.portfolioName}</h3>
                      <div className="portfolio-balance">
                        ${portfolio.cashAmount.toFixed(2)}
                      </div>
                    </div>
                    <Link 
                      to={`/portfolio/${portfolio.portfolioID}`}
                      className="btn-text"
                    >
                      View Details →
                    </Link>
                  </div>
                ))}
                
                {userPortfolios.length > 3 && (
                  <Link to="/portfolios" className="more-portfolios">
                    View {userPortfolios.length - 3} more portfolios
                  </Link>
                )}
              </div>
            </>
          )}
        </div>
        
        <div className="account-section preferences-section">
          <h2>Account Preferences</h2>
          <div className="preferences-grid">
            <div className="preference-item">
              <div className="preference-info">
                <h3>Email Notifications</h3>
                <p>Receive updates about your portfolio performance</p>
              </div>
              <label className="toggle-switch">
                <input type="checkbox" defaultChecked />
                <span className="toggle-slider"></span>
              </label>
            </div>
            
            <div className="preference-item">
              <div className="preference-info">
                <h3>Auto-refresh Data</h3>
                <p>Automatically refresh market data</p>
              </div>
              <label className="toggle-switch">
                <input type="checkbox" defaultChecked />
                <span className="toggle-slider"></span>
              </label>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Account;