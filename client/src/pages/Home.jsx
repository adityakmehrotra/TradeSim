import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { priceFromCents } from '../lib/format';
import './Home.css';

function Home() {
  const [instruments, setInstruments] = useState([]);

  useEffect(() => {
    const load = async () => {
      try {
        setInstruments(await api.getInstruments());
      } catch {
        // The market list refreshes on the next tick.
      }
    };
    load();
    const interval = setInterval(load, 2000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="home">
      <section className="home-hero">
        <h1>A simulated exchange you can actually trade against.</h1>
        <p>
          Every symbol runs its own limit order book with bot market makers quoting live. Send
          market and limit orders, watch them fill against the book, and track your positions and
          profit. No account needed.
        </p>
        <Link to="/portfolios" className="home-cta">
          Open a portfolio
        </Link>
      </section>

      <section className="home-market">
        <div className="home-market-head">
          <span>Symbol</span>
          <span>Name</span>
          <span>Last</span>
        </div>
        {instruments.map((instrument) => (
          <Link
            className="home-market-row"
            key={instrument.symbol}
            to={`/stock/${instrument.symbol}`}
          >
            <span className="sym">{instrument.symbol}</span>
            <span className="nm">{instrument.name}</span>
            <span className="last num">{priceFromCents(instrument.lastCents)}</span>
          </Link>
        ))}
      </section>
    </div>
  );
}

export default Home;
