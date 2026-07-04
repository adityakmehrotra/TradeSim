import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/common/Navbar';
import Home from './pages/Home';
import StockDetail from './pages/StockDetail';
import PortfolioList from './pages/PortfolioList';
import Portfolio from './pages/Portfolio';
import LoginForm from './components/auth/LoginForm';
import SignupForm from './components/auth/SignupForm';
import Account from './components/account/Account';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Navbar />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/stock/:symbol" element={<StockDetail />} />
          <Route path="/portfolios" element={<PortfolioList />} />
          <Route path="/portfolio/:id" element={<Portfolio />} />
          <Route path="/login" element={<LoginForm />} />
          <Route path="/signup" element={<SignupForm />} />
          <Route path="/account" element={<Account />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;