import { useState, useEffect, useRef } from 'react';
import './StockChart.css';

function StockChart({ symbol, initialPrice = null }) {
  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5001';

  const [timeRange, setTimeRange] = useState('1W');
  const [chartData, setChartData] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [hoverInfo, setHoverInfo] = useState(null);
  const chartContainerRef = useRef(null);
  const svgContainerRef = useRef(null);
  
  const formatDate = (date) => {
    return date.toISOString().split('T')[0];
  };
  
  const getDateRange = (range) => {
    const today = new Date();
    const endDate = formatDate(today);
    
    let startDate;
    let multiplier = 1;
    let timespan = 'day';
    
    switch(range) {
      case '1D':
        timespan = 'minute';
        multiplier = 5;
        const yesterday = new Date(today);
        yesterday.setDate(today.getDate() - 1);
        startDate = formatDate(yesterday);
        break;
      case '1W':
        const oneWeekAgo = new Date(today);
        oneWeekAgo.setDate(today.getDate() - 7);
        startDate = formatDate(oneWeekAgo);
        break;
      case '1M':
        const oneMonthAgo = new Date(today);
        oneMonthAgo.setMonth(today.getMonth() - 1);
        startDate = formatDate(oneMonthAgo);
        break;
      case '3M':
        const threeMonthsAgo = new Date(today);
        threeMonthsAgo.setMonth(today.getMonth() - 3);
        startDate = formatDate(threeMonthsAgo);
        break;
      case 'YTD':
        const startOfYear = new Date(today.getFullYear(), 0, 1);
        startDate = formatDate(startOfYear);
        break;
      case '1Y':
        const oneYearAgo = new Date(today);
        oneYearAgo.setFullYear(today.getFullYear() - 1);
        startDate = formatDate(oneYearAgo);
        break;
      case '5Y':
        multiplier = 7;
        const fiveYearsAgo = new Date(today);
        fiveYearsAgo.setFullYear(today.getFullYear() - 5);
        startDate = formatDate(fiveYearsAgo);
        break;
      default:
        const defaultWeekAgo = new Date(today);
        defaultWeekAgo.setDate(today.getDate() - 7);
        startDate = formatDate(defaultWeekAgo);
    }
    
    return { startDate, endDate, multiplier, timespan };
  };

  const fetchChartData = async (range) => {
    if (!symbol) return;
    
    setIsLoading(true);
    setError(null);
    
    try {
      const { startDate, endDate, multiplier, timespan } = getDateRange(range);
      
      const response = await fetch(
        `${API_BASE_URL}/paper_trader/polygon/chart?ticker=${symbol}&multiplier=${multiplier}&timespan=${timespan}&from=${startDate}&to=${endDate}`
      );
      
      if (!response.ok) {
        throw new Error(`Failed to fetch chart data: ${response.status}`);
      }
      
      const data = await response.json();
      
      if (!data.results || data.results.length === 0) {
        throw new Error('No chart data available for this time range');
      }
      
      const formattedData = data.results.map(item => ({
        timestamp: item.t,
        price: item.c
      }));
      
      setChartData(formattedData);
    } catch (err) {
      console.error('Error fetching chart data:', err);
      setError(err.message || 'Failed to load chart data');
      
      if (initialPrice !== null) {
        const today = new Date();
        setChartData([{
          timestamp: today.getTime() - 86400000,
          price: initialPrice * 0.995
        }, {
          timestamp: today.getTime(),
          price: initialPrice
        }]);
      } else {
        setChartData([]);
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchChartData(timeRange);
  }, [timeRange, symbol]);
  
  const handleMouseMove = (e) => {
    if (chartData.length === 0 || !svgContainerRef.current) return;
    
    const svgRect = svgContainerRef.current.getBoundingClientRect();
    const mouseX = e.clientX - svgRect.left;
    const chartWidth = svgRect.width;
    
    const dataIndex = Math.min(
      Math.max(
        0, 
        Math.round((mouseX / chartWidth) * (chartData.length - 1))
      ), 
      chartData.length - 1
    );
    
    const dataPoint = chartData[dataIndex];
    const xPosition = (dataIndex / (chartData.length - 1)) * 100;
    
    setHoverInfo({
      price: dataPoint.price,
      timestamp: dataPoint.timestamp,
      xPosition
    });
  };
  
  const handleMouseLeave = () => {
    setHoverInfo(null);
  };
  
  const formatTime = (timestamp) => {
    const date = new Date(timestamp);
    
    if (timeRange === '1D') {
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } else if (timeRange === '1W' || timeRange === '1M') {
      return date.toLocaleDateString([], { month: 'short', day: 'numeric' });
    } else if (timeRange === '3M' || timeRange === '1Y' || timeRange === 'YTD') {
      return date.toLocaleDateString([], { month: 'short', year: 'numeric' });
    } else {
      return date.toLocaleDateString([], { year: 'numeric' });
    }
  };
  
  if (isLoading) {
    return (
      <div className="stock-chart">
        <div className="chart-header">
          <h3>Price Chart</h3>
          <div className="time-range-selector">
            {['1D', '1W', '1M', '3M', 'YTD', '1Y', '5Y'].map(range => (
              <button
                key={range}
                className={`range-button ${timeRange === range ? 'active' : ''}`}
                onClick={() => setTimeRange(range)}
                disabled={isLoading}
              >
                {range}
              </button>
            ))}
          </div>
        </div>
        <div className="chart-loading">
          <div className="chart-loader"></div>
        </div>
      </div>
    );
  }
  
  if (error && !chartData.length) {
    return (
      <div className="stock-chart">
        <div className="chart-header">
          <h3>Price Chart</h3>
          <div className="time-range-selector">
            {['1D', '1W', '1M', '3M', 'YTD', '1Y', '5Y'].map(range => (
              <button
                key={range}
                className={`range-button ${timeRange === range ? 'active' : ''}`}
                onClick={() => setTimeRange(range)}
              >
                {range}
              </button>
            ))}
          </div>
        </div>
        <div className="chart-error">
          <p>{error}</p>
          <button className="retry-button" onClick={() => fetchChartData(timeRange)}>
            Retry
          </button>
        </div>
      </div>
    );
  }
  
  if (!chartData.length) {
    return (
      <div className="stock-chart">
        <div className="chart-header">
          <h3>Price Chart</h3>
          <div className="time-range-selector">
            {['1D', '1W', '1M', '3M', 'YTD', '1Y', '5Y'].map(range => (
              <button
                key={range}
                className={`range-button ${timeRange === range ? 'active' : ''}`}
                onClick={() => setTimeRange(range)}
              >
                {range}
              </button>
            ))}
          </div>
        </div>
        <div className="chart-placeholder">No data available for this time range</div>
      </div>
    );
  }
  
  const maxPrice = Math.max(...chartData.map(d => d.price)) * 1.01;
  const minPrice = Math.min(...chartData.map(d => d.price)) * 0.99;
  const priceRange = maxPrice - minPrice;
  const priceChange = chartData[chartData.length - 1].price - chartData[0].price;
  const chartAreaColor = priceChange >= 0 ? 'rgba(16, 185, 129, 0.1)' : 'rgba(239, 68, 68, 0.1)';
  
  const chartWidth = 100;
  const chartHeight = 400;
  const xStep = chartWidth / (chartData.length - 1);
  
  const normalizeY = (price) => {
    return chartHeight - ((price - minPrice) / priceRange) * chartHeight;
  };
  
  let linePath = `M 0 ${normalizeY(chartData[0].price)}`;
  chartData.forEach((point, i) => {
    if (i === 0) return;
    linePath += ` L ${i * xStep} ${normalizeY(point.price)}`;
  });
  
  let areaPath = `M 0 ${normalizeY(chartData[0].price)}`;
  chartData.forEach((point, i) => {
    if (i === 0) return;
    areaPath += ` L ${i * xStep} ${normalizeY(point.price)}`;
  });
  areaPath += ` L ${(chartData.length - 1) * xStep} ${chartHeight} L 0 ${chartHeight} Z`;
  
  return (
    <div className="stock-chart">
      <div className="chart-header">
        <div className="chart-title">
          <h3>Price Chart</h3>
          <div className="chart-price-info">
            <span className="current-value">
              ${hoverInfo ? hoverInfo.price.toFixed(2) : chartData[chartData.length - 1].price.toFixed(2)}
            </span>
            <span className={`price-diff ${priceChange >= 0 ? 'positive' : 'negative'}`}>
              {priceChange >= 0 ? '+' : ''}{priceChange.toFixed(2)} 
              ({((priceChange / chartData[0].price) * 100).toFixed(2)}%)
            </span>
            {hoverInfo && (
              <span className="hover-date">{formatTime(hoverInfo.timestamp)}</span>
            )}
          </div>
        </div>
        <div className="time-range-selector">
          {['1D', '1W', '1M', '3M', 'YTD', '1Y', '5Y'].map(range => (
            <button
              key={range}
              className={`range-button ${timeRange === range ? 'active' : ''}`}
              onClick={() => setTimeRange(range)}
            >
              {range}
            </button>
          ))}
        </div>
      </div>
      
      <div 
        className="chart-container" 
        ref={chartContainerRef}
        onMouseMove={handleMouseMove}
        onMouseLeave={handleMouseLeave}
      >
        <div className="chart-y-labels">
          <div className="y-label top">${maxPrice.toFixed(2)}</div>
          <div className="y-label middle">${((maxPrice + minPrice) / 2).toFixed(2)}</div>
          <div className="y-label bottom">${minPrice.toFixed(2)}</div>
        </div>
        
        <div className="chart-svg-container" ref={svgContainerRef}>
          <svg 
            width="100%" 
            height="100%" 
            viewBox={`0 0 ${chartWidth} ${chartHeight}`} 
            preserveAspectRatio="none"
            className="chart-svg"
            shapeRendering="geometricPrecision"
          >
            {[0, 0.25, 0.5, 0.75, 1].map((ratio, i) => {
              const y = chartHeight * ratio;
              return (
                <line 
                  key={i}
                  x1="0" 
                  y1={y} 
                  x2={chartWidth} 
                  y2={y} 
                  stroke="var(--chart-grid, rgba(203, 213, 225, 0.5))" 
                  strokeWidth="1"
                  strokeDasharray={i === 0 || i === 4 ? "none" : "3,3"}
                />
              );
            })}
            
            <path 
              d={areaPath} 
              fill={chartAreaColor} 
              className="chart-area" 
            />
            
            <path 
              d={linePath} 
              fill="none" 
              stroke={`var(${priceChange >= 0 ? '--success-color' : '--error-color'})`}
              strokeWidth="2"
              strokeLinejoin="round"
              strokeLinecap="round"
              className="chart-line"
            />
            
            {[0, 0.25, 0.5, 0.75, 1].map((ratio, i) => {
              const index = Math.min(Math.floor((chartData.length - 1) * ratio), chartData.length - 1);
              const x = index * xStep;
              const y = normalizeY(chartData[index].price);
              return (
                <circle 
                  key={i}
                  cx={x} 
                  cy={y} 
                  r="3" 
                  fill={`var(${priceChange >= 0 ? '--success-color' : '--error-color'})`}
                  className="chart-point"
                />
              );
            })}
            
            <circle 
              cx={(chartData.length - 1) * xStep} 
              cy={normalizeY(chartData[chartData.length - 1].price)} 
              r="5" 
              fill={`var(${priceChange >= 0 ? '--success-color' : '--error-color'})`}
              stroke="white"
              strokeWidth="1.5"
              className="chart-end-point"
            />
          </svg>
          
          {hoverInfo && (
            <div 
              className="hover-line" 
              style={{ left: `${hoverInfo.xPosition}%` }}
            >
              <div className="hover-price-indicator">
                ${hoverInfo.price.toFixed(2)}
              </div>
            </div>
          )}
        </div>
        
        <div className="chart-x-labels">
          {chartData.length > 1 && [0, 0.25, 0.5, 0.75, 1].map((ratio, i) => {
            const index = Math.min(Math.round((chartData.length - 1) * ratio), chartData.length - 1);
            return (
              <div key={i} className="x-label" style={{ left: `${ratio * 100}%` }}>
                {formatTime(chartData[index]?.timestamp)}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

export default StockChart;