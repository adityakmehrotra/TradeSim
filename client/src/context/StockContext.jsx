import { createContext, useState } from 'react';

export const StockContext = createContext();

export const StockProvider = ({ children }) => {
  const [watchlist, setWatchlist] = useState([]);
  const [portfolios, setPortfolios] = useState([]);
  const [selectedStock, setSelectedStock] = useState(null);
  
  const addToWatchlist = (stock) => {
    if (!watchlist.some(item => item.symbol === stock.symbol)) {
      setWatchlist([...watchlist, stock]);
    }
  };
  
  const removeFromWatchlist = (symbol) => {
    setWatchlist(watchlist.filter(stock => stock.symbol !== symbol));
  };

  return (
    <StockContext.Provider value={{ 
      watchlist, 
      addToWatchlist, 
      removeFromWatchlist,
      portfolios,
      setPortfolios,
      selectedStock,
      setSelectedStock
    }}>
      {children}
    </StockContext.Provider>
  );
};