import { priceFromCents, shares } from '../../lib/format';

function OrderBookLadder({ bids, asks }) {
  const topAsks = asks.slice(0, 8);
  const topBids = bids.slice(0, 8);
  const maxSize = Math.max(
    1,
    ...topAsks.map((level) => level.quantity),
    ...topBids.map((level) => level.quantity)
  );

  const bestBid = bids[0]?.priceCents;
  const bestAsk = asks[0]?.priceCents;
  const spread = bestBid != null && bestAsk != null ? bestAsk - bestBid : null;

  return (
    <div className="panel term-book">
      <div className="panel-title">Order Book</div>
      <div className="ladder">
        {[...topAsks].reverse().map((level, index) => (
          <div className="ladder-row ask" key={`a${index}`}>
            <span className="price num">{priceFromCents(level.priceCents)}</span>
            <span className="size num">{shares(level.quantity)}</span>
            <span className="depth-bar" style={{ width: `${(level.quantity / maxSize) * 100}%` }} />
          </div>
        ))}

        <div className="ladder-mid">
          <span>Spread</span>
          <span className="spread num">{spread != null ? priceFromCents(spread) : '-'}</span>
        </div>

        {topBids.map((level, index) => (
          <div className="ladder-row bid" key={`b${index}`}>
            <span className="price num">{priceFromCents(level.priceCents)}</span>
            <span className="size num">{shares(level.quantity)}</span>
            <span className="depth-bar" style={{ width: `${(level.quantity / maxSize) * 100}%` }} />
          </div>
        ))}
      </div>
    </div>
  );
}

export default OrderBookLadder;
