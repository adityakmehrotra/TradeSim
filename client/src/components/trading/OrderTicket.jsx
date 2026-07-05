import { useState } from 'react';
import { Link } from 'react-router-dom';
import { priceFromCents, usd } from '../../lib/format';
import api from '../../services/api';

function OrderTicket({ symbol, lastCents, portfolio, onPlaced }) {
  const [side, setSide] = useState('BUY');
  const [type, setType] = useState('MARKET');
  const [quantity, setQuantity] = useState('10');
  const [limit, setLimit] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  if (!portfolio) {
    return (
      <div className="panel term-ticket">
        <div className="panel-title">Order</div>
        <div className="ticket-note">
          You need a portfolio before you can trade. <Link to="/portfolios">Create one</Link> to get
          your starting cash.
        </div>
      </div>
    );
  }

  const quantityNumber = parseInt(quantity, 10) || 0;
  const limitCents = type === 'LIMIT' ? Math.round(parseFloat(limit || '0') * 100) : null;
  const referenceCents = type === 'LIMIT' ? limitCents || 0 : lastCents || 0;
  const estimate = (quantityNumber * referenceCents) / 100;
  const buyingPower = (portfolio.cash || 0) - (portfolio.reservedCash || 0);

  const submit = async () => {
    setError(null);
    if (quantityNumber <= 0) {
      setError('Enter a quantity.');
      return;
    }
    if (type === 'LIMIT' && (!limitCents || limitCents <= 0)) {
      setError('Enter a limit price.');
      return;
    }

    try {
      setSubmitting(true);
      await api.placeOrder({
        portfolioID: portfolio.portfolioID,
        symbol,
        side,
        type,
        limitPriceCents: type === 'LIMIT' ? limitCents : null,
        quantity: quantityNumber,
      });
      onPlaced?.();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="panel term-ticket">
      <div className="panel-title">Order</div>
      <div className="ticket">
        <div className="side-toggle">
          <button
            className={`buy ${side === 'BUY' ? 'active' : ''}`}
            onClick={() => setSide('BUY')}
          >
            Buy
          </button>
          <button
            className={`sell ${side === 'SELL' ? 'active' : ''}`}
            onClick={() => setSide('SELL')}
          >
            Sell
          </button>
        </div>

        <div className="type-tabs">
          <button className={type === 'MARKET' ? 'active' : ''} onClick={() => setType('MARKET')}>
            Market
          </button>
          <button className={type === 'LIMIT' ? 'active' : ''} onClick={() => setType('LIMIT')}>
            Limit
          </button>
        </div>

        <div className="ticket-field">
          <label>Quantity (shares)</label>
          <input
            value={quantity}
            inputMode="numeric"
            onChange={(event) => setQuantity(event.target.value.replace(/[^\d]/g, ''))}
          />
        </div>

        {type === 'LIMIT' && (
          <div className="ticket-field">
            <label>Limit price ($)</label>
            <input
              value={limit}
              inputMode="decimal"
              placeholder={priceFromCents(lastCents)}
              onChange={(event) => setLimit(event.target.value.replace(/[^\d.]/g, ''))}
            />
          </div>
        )}

        <div className="ticket-row">
          <span>{type === 'LIMIT' ? 'Order value' : 'Estimated cost'}</span>
          <span className="val">{usd(estimate)}</span>
        </div>
        <div className="ticket-row">
          <span>Buying power</span>
          <span className="val">{usd(buyingPower)}</span>
        </div>

        <button
          className={`ticket-submit ${side === 'BUY' ? 'buy' : 'sell'}`}
          onClick={submit}
          disabled={submitting}
        >
          {submitting ? 'Placing...' : `${side === 'BUY' ? 'Buy' : 'Sell'} ${symbol}`}
        </button>
        {error && <div className="ticket-error">{error}</div>}
      </div>
    </div>
  );
}

export default OrderTicket;
