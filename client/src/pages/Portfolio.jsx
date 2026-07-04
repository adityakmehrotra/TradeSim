import { useState, useEffect, useContext } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import api from '../services/api';
import './Portfolio.css';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

function Portfolio() {
  const { id } = useParams();
  const { user } = useContext(AuthContext);
  const [portfolio, setPortfolio] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('holdings');
  const navigate = useNavigate();

  const [performanceData] = useState({
    labels: ['7 Days Ago', '6 Days Ago', '5 Days Ago', '4 Days Ago', '3 Days Ago', 'Yesterday', 'Today'],
    datasets: [
      {
        label: 'Portfolio Value',
        data: [],
        fill: false,
        borderColor: 'rgb(59, 130, 246)',
        tension: 0.1,
        pointRadius: 4,
        pointBackgroundColor: 'rgb(59, 130, 246)',
      },
    ],
  });

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }

    const fetchPortfolioDetails = async () => {
      try {
        setLoading(true);
        
        const portfolioData = await api.getPortfolio(id);
        
        if (portfolioData.accountID !== user.accountId) {
          setError("You don't have permission to view this portfolio");
          setLoading(false);
          return;
        }
        
        const cashAmount = portfolioData.cashAmount || 0;
        const totalValue = portfolioData.initialBalance || 0;
        const investedAmount = totalValue - cashAmount;
        
        const processed = {
          ...portfolioData,
          investedAmount,
          holdings: [],
          transactions: portfolioData.transactionList || []
        };
        
        if (portfolioData.assets) {
          processed.holdings = Object.entries(portfolioData.assets)
            .filter(([symbol]) => symbol !== 'Cash')
            .map(([symbol, asset]) => {
              const currentPrice = asset.currentPrice || asset.initialPrice;
              const totalValue = asset.sharesOwned * currentPrice;
              const costBasis = asset.initialCashInvestment || (asset.sharesOwned * asset.initialPrice);
              const percentChange = ((totalValue - costBasis) / costBasis) * 100;
              
              return {
                symbol,
                name: asset.companyName || symbol,
                shares: asset.sharesOwned,
                averageCost: (costBasis / asset.sharesOwned),
                currentPrice,
                totalValue,
                percentChange
              };
            });
        }
        
        const initialBalance = portfolioData.initialBalance;
        const randomPerformanceData = [
          initialBalance,
          initialBalance * (1 + (Math.random() * 0.02 - 0.01)),
          initialBalance * (1 + (Math.random() * 0.04 - 0.02)),
          initialBalance * (1 + (Math.random() * 0.06 - 0.03)),
          initialBalance * (1 + (Math.random() * 0.08 - 0.04)),
          initialBalance * (1 + (Math.random() * 0.1 - 0.05)),
          totalValue
        ];
        
        performanceData.datasets[0].data = randomPerformanceData;
        
        setPortfolio(processed);
      } catch (err) {
        console.error('Error fetching portfolio details:', err);
        setError('Failed to load portfolio details. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    fetchPortfolioDetails();
  }, [id, user, navigate, performanceData]);

  if (loading) {
    return (
      <div className="portfolio-page">
        <div className="loading-spinner">Loading portfolio details...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="portfolio-page">
        <div className="error-message">{error}</div>
        <Link to="/portfolios" className="btn btn-primary mt-4">
          Back to Portfolios
        </Link>
      </div>
    );
  }

  if (!portfolio) {
    return (
      <div className="portfolio-page">
        <div className="error-message">Portfolio not found</div>
        <Link to="/portfolios" className="btn btn-primary mt-4">
          Back to Portfolios
        </Link>
      </div>
    );
  }

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false,
      },
      tooltip: {
        callbacks: {
          label: function(context) {
            return `$${context.parsed.y.toFixed(2)}`;
          }
        }
      }
    },
    scales: {
      y: {
        ticks: {
          callback: function(value) {
            return '$' + value.toFixed(0);
          }
        }
      }
    }
  };

  const portfolioValue = portfolio.cashAmount + portfolio.holdings.reduce(
    (total, holding) => total + holding.totalValue, 0
  );
  
  const gainLoss = portfolioValue - portfolio.initialBalance;
  const gainLossPercent = (gainLoss / portfolio.initialBalance) * 100;

  return (
    <div className="portfolio-page">
      <div className="portfolio-header">
        <div className="portfolio-title">
          <Link to="/portfolios" className="back-link">
            ← All Portfolios
          </Link>
          <h1>{portfolio.portfolioName}</h1>
        </div>
        <div className="portfolio-actions">
          <Link to={`/trade/${portfolio.portfolioID}`} className="btn btn-primary">
            Trade
          </Link>
        </div>
      </div>
      
      {/* Portfolio Summary Cards */}
      <div className="portfolio-summary-grid">
        <div className="summary-card">
          <div className="summary-card-title">Portfolio Value</div>
          <div className="summary-card-value">${portfolioValue.toFixed(2)}</div>
          <div className={`summary-card-change ${gainLoss >= 0 ? 'positive' : 'negative'}`}>
            {gainLoss >= 0 ? '+' : ''}{gainLoss.toFixed(2)} ({gainLossPercent.toFixed(2)}%)
          </div>
        </div>
        
        <div className="summary-card">
          <div className="summary-card-title">Initial Balance</div>
          <div className="summary-card-value">${portfolio.initialBalance.toFixed(2)}</div>
        </div>
        
        <div className="summary-card">
          <div className="summary-card-title">Invested</div>
          <div className="summary-card-value">${portfolio.investedAmount.toFixed(2)}</div>
          <div className="summary-card-subtitle">
            {((portfolio.investedAmount / portfolio.initialBalance) * 100).toFixed(2)}% of portfolio
          </div>
        </div>
        
        <div className="summary-card">
          <div className="summary-card-title">Cash</div>
          <div className="summary-card-value">${portfolio.cashAmount.toFixed(2)}</div>
          <div className="summary-card-subtitle">
            {((portfolio.cashAmount / portfolio.initialBalance) * 100).toFixed(2)}% of portfolio
          </div>
        </div>
      </div>
      
      {/* Performance Chart */}
      <div className="portfolio-chart-container">
        <h2>Performance</h2>
        <div className="portfolio-chart">
          <Line data={performanceData} options={chartOptions} />
        </div>
      </div>
      
      {/* Holdings and Transactions Tabs */}
      <div className="portfolio-tabs">
        <button 
          className={`tab-button ${activeTab === 'holdings' ? 'active' : ''}`}
          onClick={() => setActiveTab('holdings')}
        >
          Holdings
        </button>
        <button 
          className={`tab-button ${activeTab === 'transactions' ? 'active' : ''}`}
          onClick={() => setActiveTab('transactions')}
        >
          Transactions
        </button>
      </div>
      
      {/* Holdings Table */}
      {activeTab === 'holdings' && (
        <div className="table-container">
          {portfolio.holdings.length === 0 ? (
            <div className="empty-state">
              <p>You don't have any holdings in this portfolio yet.</p>
              <Link to={`/trade/${portfolio.portfolioID}`} className="btn btn-primary">
                Start Trading
              </Link>
            </div>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>Symbol</th>
                  <th>Name</th>
                  <th>Shares</th>
                  <th>Avg. Cost</th>
                  <th>Current Price</th>
                  <th>Value</th>
                  <th>% Change</th>
                </tr>
              </thead>
              <tbody>
                {portfolio.holdings.map((holding) => (
                  <tr key={holding.symbol}>
                    <td className="symbol-cell">{holding.symbol}</td>
                    <td>{holding.name}</td>
                    <td>{holding.shares.toFixed(2)}</td>
                    <td>${holding.averageCost.toFixed(2)}</td>
                    <td>${holding.currentPrice.toFixed(2)}</td>
                    <td>${holding.totalValue.toFixed(2)}</td>
                    <td className={holding.percentChange >= 0 ? 'positive' : 'negative'}>
                      {holding.percentChange >= 0 ? '+' : ''}{holding.percentChange.toFixed(2)}%
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
      
      {/* Transactions Table */}
      {activeTab === 'transactions' && (
        <div className="table-container">
          {portfolio.transactions.length === 0 ? (
            <div className="empty-state">
              <p>No transaction history available for this portfolio.</p>
              <Link to={`/trade/${portfolio.portfolioID}`} className="btn btn-primary">
                Start Trading
              </Link>
            </div>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Type</th>
                  <th>Symbol</th>
                  <th>Shares</th>
                  <th>Price</th>
                  <th>Total</th>
                </tr>
              </thead>
              <tbody>
                {portfolio.transactions.map((transaction, index) => (
                  <tr key={index}>
                    <td>{new Date(transaction.timestamp).toLocaleDateString()}</td>
                    <td className={transaction.type.toLowerCase() === 'buy' ? 'buy-cell' : 'sell-cell'}>
                      {transaction.type}
                    </td>
                    <td className="symbol-cell">{transaction.symbol}</td>
                    <td>{transaction.shares.toFixed(2)}</td>
                    <td>${transaction.price.toFixed(2)}</td>
                    <td>${(transaction.shares * transaction.price).toFixed(2)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
}

export default Portfolio;