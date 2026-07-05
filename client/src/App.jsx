import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/common/Navbar';
import Home from './pages/Home';
import TradingScreen from './pages/TradingScreen';
import PortfolioList from './pages/PortfolioList';
import Portfolio from './pages/Portfolio';
import Account from './components/account/Account';

function App() {
  return (
    <Router>
      <Navbar />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/stock/:symbol" element={<TradingScreen />} />
        <Route path="/portfolios" element={<PortfolioList />} />
        <Route path="/portfolio/:id" element={<Portfolio />} />
        <Route path="/account" element={<Account />} />
      </Routes>
    </Router>
  );
}

export default App;
