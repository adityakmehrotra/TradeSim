import { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import { useSession } from '../context/SessionContext';
import api from '../services/api';
import CandleChart from '../components/trading/CandleChart';
import OrderBookLadder from '../components/trading/OrderBookLadder';
import TradeTape from '../components/trading/TradeTape';
import OrderTicket from '../components/trading/OrderTicket';
import { priceFromCents, signedUsd, signedPercent, shares, usd } from '../lib/format';
import './TradingScreen.css';

function TradingScreen() {
  const { symbol } = useParams();
  const { portfolios, refresh } = useSession();
  const portfolio = portfolios[0] || null;

  const [quote, setQuote] = useState(null);
  const [depth, setDepth] = useState({ bids: [], asks: [] });
  const [trades, setTrades] = useState([]);
  const [candles, setCandles] = useState([]);
  const [positions, setPositions] = useState([]);
  const [openOrders, setOpenOrders] = useState([]);

  const loadMarket = useCallback(async () => {
    try {
      const [nextQuote, nextDepth, nextTrades, nextCandles] = await Promise.all([
        api.getQuote(symbol),
        api.getDepth(symbol),
        api.getTrades(symbol),
        api.getCandles(symbol),
      ]);
      setQuote(nextQuote);
      setDepth(nextDepth);
      setTrades(nextTrades);
      setCandles(nextCandles);
    } catch {
      // Transient poll failures are ignored; the next tick recovers.
    }
  }, [symbol]);

  const loadAccount = useCallback(async () => {
    try {
      setOpenOrders(await api.getOpenOrders());
      if (portfolio) {
        setPositions(await api.getPositions(portfolio.portfolioID));
      }
    } catch {
      // Ignore; account panels refresh on the next tick.
    }
  }, [portfolio]);

  useEffect(() => {
    loadMarket();
    const interval = setInterval(loadMarket, 1000);
    return () => clearInterval(interval);
  }, [loadMarket]);

  useEffect(() => {
    loadAccount();
    const interval = setInterval(loadAccount, 2500);
    return () => clearInterval(interval);
  }, [loadAccount]);

  const onPlaced = () => {
    loadAccount();
    refresh();
  };

  const cancelOrder = async (orderId) => {
    await api.cancelOrder(orderId);
    loadAccount();
  };

  const last = quote?.lastCents;
  const open = candles[0]?.openCents ?? last;
  const change = last != null && open != null ? (last - open) / 100 : null;
  const changePercent = last != null && open ? ((last - open) / open) * 100 : null;
  const rising = (change ?? 0) >= 0;

  const sessionHigh = candles.length ? Math.max(...candles.map((c) => c.highCents)) : null;
  const sessionLow = candles.length ? Math.min(...candles.map((c) => c.lowCents)) : null;
  const sessionVolume = candles.reduce((total, c) => total + c.volume, 0);

  return (
    <div className="terminal">
      <div className="term-header">
        <span className="term-symbol">{symbol}</span>
        <span className="term-name">{quote?.name || ''}</span>
        <div className="term-stats">
          <div className="term-stat">
            <span className="term-stat-label">Last</span>
            <span className={`term-stat-value num ${rising ? 'up' : 'down'}`}>
              {priceFromCents(last)}
            </span>
          </div>
          <div className="term-stat">
            <span className="term-stat-label">Change</span>
            <span className={`term-stat-value num ${rising ? 'up' : 'down'}`}>
              {signedUsd(change)} ({signedPercent(changePercent)})
            </span>
          </div>
          <div className="term-stat">
            <span className="term-stat-label">High</span>
            <span className="term-stat-value num">{priceFromCents(sessionHigh)}</span>
          </div>
          <div className="term-stat">
            <span className="term-stat-label">Low</span>
            <span className="term-stat-value num">{priceFromCents(sessionLow)}</span>
          </div>
          <div className="term-stat">
            <span className="term-stat-label">Volume</span>
            <span className="term-stat-value num">{shares(sessionVolume)}</span>
          </div>
        </div>
      </div>

      <div className="term-grid">
        <div className="panel term-chart">
          <div className="panel-title">
            {symbol} <span className="panel-subtitle">5s candles</span>
          </div>
          <CandleChart candles={candles} lastCents={last} />
        </div>

        <OrderBookLadder bids={depth.bids} asks={depth.asks} />
        <OrderTicket symbol={symbol} lastCents={last} portfolio={portfolio} onPlaced={onPlaced} />
        <TradeTape trades={trades} />

        <div className="term-lower">
          <PositionsPanel positions={positions} />
          <OpenOrdersPanel orders={openOrders} onCancel={cancelOrder} />
        </div>
      </div>
    </div>
  );
}

function PositionsPanel({ positions }) {
  return (
    <div className="panel">
      <div className="panel-title">Positions</div>
      <table className="dtable">
        <thead>
          <tr>
            <th>Symbol</th>
            <th>Qty</th>
            <th>Last</th>
            <th>Value</th>
            <th>Unrealized</th>
            <th>Realized</th>
          </tr>
        </thead>
        <tbody>
          {positions.length === 0 ? (
            <tr>
              <td className="empty" colSpan={6}>
                no open positions
              </td>
            </tr>
          ) : (
            positions.map((position) => (
              <tr key={position.symbol}>
                <td>{position.symbol}</td>
                <td>{shares(position.quantity)}</td>
                <td>{priceFromCents(position.lastCents)}</td>
                <td>{usd(position.marketValue)}</td>
                <td className={position.unrealizedPnl >= 0 ? 'up' : 'down'}>
                  {signedUsd(position.unrealizedPnl)}
                </td>
                <td className={position.realizedPnl >= 0 ? 'up' : 'down'}>
                  {signedUsd(position.realizedPnl)}
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}

function OpenOrdersPanel({ orders, onCancel }) {
  return (
    <div className="panel">
      <div className="panel-title">Open Orders</div>
      <table className="dtable">
        <thead>
          <tr>
            <th>Symbol</th>
            <th>Side</th>
            <th>Type</th>
            <th>Limit</th>
            <th>Remaining</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {orders.length === 0 ? (
            <tr>
              <td className="empty" colSpan={6}>
                no open orders
              </td>
            </tr>
          ) : (
            orders.map((order) => (
              <tr key={order.orderId}>
                <td>{order.symbol}</td>
                <td className={order.side === 'BUY' ? 'up' : 'down'}>{order.side}</td>
                <td>{order.type}</td>
                <td>{order.type === 'LIMIT' ? priceFromCents(order.limitPriceCents) : '-'}</td>
                <td>{shares(order.remaining)}</td>
                <td>
                  <button className="cancel-btn" onClick={() => onCancel(order.orderId)}>
                    Cancel
                  </button>
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}

export default TradingScreen;
