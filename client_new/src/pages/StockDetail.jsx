import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import StockChart from '../components/trading/StockChart';
import TradeForm from '../components/trading/TradeForm';
import './StockDetail.css';

function StockDetail() {
  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5001';
  const { symbol } = useParams();
  const navigate = useNavigate();
  const [stockPrice, setStockPrice] = useState(null);
  const [companyData, setCompanyData] = useState(null);
  const [keyStats, setKeyStats] = useState(null);
  const [chartData, setChartData] = useState(null);
  const [newsData, setNewsData] = useState([]);
  const [loading, setLoading] = useState({
    price: true,
    company: true,
    stats: true,
    chart: true,
    news: true
  });
  const [error, setError] = useState(null);
  
  useEffect(() => {
    console.log("Symbol from params:", symbol);
    if (!symbol) {
      console.error("No stock symbol provided in URL");
      navigate("/market", { replace: true });
    }
  }, [symbol, navigate]);

  const formatDate = (date) => {
    return date.toISOString().split('T')[0];
  };

  const getDateRange = () => {
    const today = new Date();
    const oneYearAgo = new Date();
    oneYearAgo.setFullYear(today.getFullYear() - 1);
    
    return {
      from: formatDate(oneYearAgo),
      to: formatDate(today)
    };
  };

  const fetchStockPrice = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/paper_trader/polygon/price?ticker=${symbol}`);
      if (!response.ok) {
        throw new Error(`Failed to fetch price data: ${response.statusText}`);
      }
      const data = await response.json();
      
      if (data.ticker && data.ticker.lastTrade) {
        setStockPrice({
          price: data.ticker.lastTrade.p,
          previousClose: data.ticker.prevDay.c,
          change: data.ticker.todaysChange,
          changePercent: data.ticker.todaysChangePerc
        });
      } else {
        const lastPrice = data.ticker?.day?.c || 
                         data.ticker?.lastQuote?.p || 
                         data.results?.lastTrade?.p || 
                         0;
        const prevClose = data.ticker?.prevDay?.c || 
                         data.results?.previousClose || 
                         0;
        const change = lastPrice - prevClose;
        const changePercent = prevClose ? (change / prevClose) * 100 : 0;
        
        setStockPrice({
          price: lastPrice,
          previousClose: prevClose,
          change,
          changePercent
        });
      }
    } catch (err) {
      console.error("Error fetching price:", err);
      setError(`Failed to load stock price: ${err.message}`);
    } finally {
      setLoading(prev => ({ ...prev, price: false }));
    }
  };

  const fetchCompanyData = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/paper_trader/polygon/companyData?ticker=${symbol}`);
      if (!response.ok) {
        throw new Error(`Failed to fetch company data: ${response.statusText}`);
      }
      const data = await response.json();
      
      if (data.results) {
        setCompanyData({
          name: data.results.name,
          description: data.results.description,
          industry: data.results.sic_description,
          exchange: data.results.primary_exchange,
          marketCap: data.results.market_cap,
          website: data.results.homepage_url,
          ceo: data.results.ceo || 'N/A',
          employees: data.results.total_employees || 'N/A',
          hq: data.results.address ? 
            `${data.results.address.city}, ${data.results.address.state}` : 'N/A'
        });
      }
    } catch (err) {
      console.error("Error fetching company data:", err);
      setError(`Failed to load company data: ${err.message}`);
    } finally {
      setLoading(prev => ({ ...prev, company: false }));
    }
  };

  const fetchKeyStatistics = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/paper_trader/polygon/keyStatistics?ticker=${symbol}`);
      if (!response.ok) {
        throw new Error(`Failed to fetch statistics: ${response.statusText}`);
      }
      const data = await response.json();
      
      if (data.results && data.results.length > 0) {
        const result = data.results[0];
        setKeyStats({
          open: result.o,
          high: result.h,
          low: result.l,
          close: result.c,
          volume: result.v,
          vwap: result.vw,
          transactions: result.n
        });
      }
    } catch (err) {
      console.error("Error fetching key statistics:", err);
      setError(`Failed to load key statistics: ${err.message}`);
    } finally {
      setLoading(prev => ({ ...prev, stats: false }));
    }
  };

  const fetchChartData = async () => {
    try {
      const dates = getDateRange();
      const response = await fetch(
        `${API_BASE_URL}/paper_trader/polygon/chart?ticker=${symbol}&multiplier=1&timespan=day&from=${dates.from}&to=${dates.to}`
      );
      if (!response.ok) {
        throw new Error(`Failed to fetch chart data: ${response.statusText}`);
      }
      const data = await response.json();
      
      if (data.results) {
        setChartData({
          labels: data.results.map(item => {
            const date = new Date(item.t);
            return date.toLocaleDateString();
          }),
          datasets: [{
            label: symbol,
            data: data.results.map(item => item.c),
            fill: false,
            borderColor: 'rgb(59, 130, 246)',
            tension: 0.1
          }]
        });
      }
    } catch (err) {
      console.error("Error fetching chart data:", err);
      setError(`Failed to load chart data: ${err.message}`);
    } finally {
      setLoading(prev => ({ ...prev, chart: false }));
    }
  };

  const fetchNewsData = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/paper_trader/polygon/news?ticker=${symbol}`);
      if (!response.ok) {
        throw new Error(`Failed to fetch news: ${response.statusText}`);
      }
      const data = await response.json();
      
      if (data.results) {
        setNewsData(data.results.slice(0, 5));
      }
    } catch (err) {
      console.error("Error fetching news:", err);
    } finally {
      setLoading(prev => ({ ...prev, news: false }));
    }
  };

  useEffect(() => {
    if (!symbol) return;
    
    setStockPrice(null);
    setCompanyData(null);
    setKeyStats(null);
    setChartData(null);
    setNewsData([]);
    setError(null);
    setLoading({
      price: true,
      company: true,
      stats: true,
      chart: true,
      news: true
    });
    
    fetchStockPrice();
    fetchCompanyData();
    fetchKeyStatistics();
    fetchChartData();
    fetchNewsData();
  }, [symbol]);

  const isMainDataLoading = loading.price || loading.company || loading.stats;
  
  if (isMainDataLoading) {
    return (
      <div className="stock-detail-page">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <div className="loading-text">Loading {symbol} data...</div>
        </div>
      </div>
    );
  }

  if (error && !stockPrice && !companyData) {
    return (
      <div className="stock-detail-page">
        <div className="error-container">
          <div className="error-icon">❌</div>
          <div className="error-message">{error}</div>
          <Link to="/market" className="btn btn-primary">
            Back to Market
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="stock-detail-page">

      {/* Stock Header with Price and Basic Info */}
      <div className="stock-header">
        <div className="stock-title">
          <h1>{symbol}</h1>
          <h2>{companyData?.name || 'Loading company name...'}</h2>
        </div>
        <div className="stock-price">
          <div className="current-price">
            ${stockPrice?.price?.toFixed(2) || '-.--'}
          </div>
          <div className={`price-change ${(stockPrice?.change || 0) >= 0 ? 'positive' : 'negative'}`}>
            {stockPrice?.change > 0 ? '+' : ''}
            {stockPrice?.change?.toFixed(2) || '0.00'} 
            ({stockPrice?.change > 0 ? '+' : ''}
            {stockPrice?.changePercent?.toFixed(2) || '0.00'}%)
          </div>
        </div>
      </div>

      <div className="stock-content">
        <div className="chart-section">

        {/* Stock Chart */}
        <div className="stock-chart-container">
          <div className="chart-header">
            <h3>Price Chart</h3>
            {loading.chart && <div className="small-spinner"></div>}
          </div>
          {error && error.includes('chart') ? (
            <div className="chart-error-message">{error}</div>
          ) : (
            <StockChart 
              symbol={symbol} 
              initialPrice={stockPrice?.price} 
            />
          )}
        </div>
          
          {/* Company Information */}
          <div className="company-info">
            <h3>Company Overview</h3>
            <div className="company-description">
              {companyData?.description || 'No company description available.'}
            </div>
            <div className="company-details">
              <div className="detail-item">
                <span className="detail-label">Industry</span>
                <span className="detail-value">{companyData?.industry || 'N/A'}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Exchange</span>
                <span className="detail-value">{companyData?.exchange || 'N/A'}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">CEO</span>
                <span className="detail-value">{companyData?.ceo || 'N/A'}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Employees</span>
                <span className="detail-value">{companyData?.employees?.toLocaleString() || 'N/A'}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Headquarters</span>
                <span className="detail-value">{companyData?.hq || 'N/A'}</span>
              </div>
              {companyData?.website && (
                <div className="detail-item">
                  <span className="detail-label">Website</span>
                  <a href={companyData.website} target="_blank" rel="noopener noreferrer" className="detail-link">
                    {companyData.website.replace(/^https?:\/\//, '').replace(/\/$/, '')}
                  </a>
                </div>
              )}
            </div>
          </div>
          
          {/* Key Statistics */}
          <div className="key-stats">
            <h3>Key Statistics</h3>
            <div className="stats-grid">
              <div className="stat-item">
                <span className="stat-label">Previous Close</span>
                <span className="stat-value">
                  ${stockPrice?.previousClose?.toFixed(2) || 'N/A'}
                </span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Open</span>
                <span className="stat-value">
                  ${keyStats?.open?.toFixed(2) || 'N/A'}
                </span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Day's Range</span>
                <span className="stat-value">
                  ${keyStats?.low?.toFixed(2) || 'N/A'} - ${keyStats?.high?.toFixed(2) || 'N/A'}
                </span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Volume</span>
                <span className="stat-value">
                  {keyStats?.volume?.toLocaleString() || 'N/A'}
                </span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Market Cap</span>
                <span className="stat-value">
                  {companyData?.marketCap ? 
                    `$${(companyData.marketCap / 1000000000).toFixed(2)}B` : 'N/A'}
                </span>
              </div>
              <div className="stat-item">
                <span className="stat-label">VWAP</span>
                <span className="stat-value">
                  ${keyStats?.vwap?.toFixed(2) || 'N/A'}
                </span>
              </div>
            </div>
          </div>

          {/* News Section */}
          {newsData.length > 0 && (
            <div className="news-section">
              <h3>Latest News</h3>
              <div className="news-list">
                {newsData.map((news, index) => (
                  <a 
                    key={index} 
                    href={news.article_url} 
                    target="_blank" 
                    rel="noopener noreferrer" 
                    className="news-item"
                  >
                    <div className="news-image">
                      {news.image_url ? (
                        <img src={news.image_url} alt={news.title} />
                      ) : (
                        <div className="placeholder-image">{symbol}</div>
                      )}
                    </div>
                    <div className="news-content">
                      <h4 className="news-title">{news.title}</h4>
                      <p className="news-source">
                        <span className="source-name">{news.publisher.name}</span>
                        <span className="news-date">
                          {new Date(news.published_utc).toLocaleDateString()}
                        </span>
                      </p>
                    </div>
                  </a>
                ))}
              </div>
            </div>
          )}
        </div>
        
        {/* Trade Form Section */}
        <div className="trade-section">
          <TradeForm 
            symbol={symbol} 
            currentPrice={stockPrice?.price} 
          />
        </div>
      </div>
    </div>
  );
}

export default StockDetail;