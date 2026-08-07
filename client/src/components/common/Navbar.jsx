import { NavLink, Link } from 'react-router-dom';
import SearchBar from './SearchBar';
import './Navbar.css';

function Navbar() {
  return (
    <nav className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="brand">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path
              d="M3 17l6-6 4 4 8-8"
              stroke="currentColor"
              strokeWidth="2.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
            <path
              d="M15 7h6v6"
              stroke="currentColor"
              strokeWidth="2.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
          <span>TradeSim</span>
        </Link>

        <div className="nav-links">
          <NavLink
            to="/"
            end
            className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
          >
            Markets
          </NavLink>
          <NavLink
            to="/portfolios"
            className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
          >
            Portfolios
          </NavLink>
          <NavLink
            to="/account"
            className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
          >
            Account
          </NavLink>
        </div>

        <div className="nav-search">
          <SearchBar />
        </div>
      </div>
    </nav>
  );
}

export default Navbar;
