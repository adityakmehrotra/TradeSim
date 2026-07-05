import { priceFromCents, shares, clockTime } from '../../lib/format';

function TradeTape({ trades }) {
  return (
    <div className="panel term-tape">
      <div className="panel-title">Time and Sales</div>
      <div className="tape">
        {trades.length === 0 ? (
          <div className="dtable">
            <div className="empty">no trades yet</div>
          </div>
        ) : (
          trades.map((trade, index) => (
            <div className="tape-row" key={index}>
              <span className={`t-price num ${trade.aggressor === 'BUY' ? 'buy' : 'sell'}`}>
                {priceFromCents(trade.priceCents)}
              </span>
              <span className="t-size num">{shares(trade.quantity)}</span>
              <span className="t-time">{clockTime(trade.epochMillis)}</span>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default TradeTape;
