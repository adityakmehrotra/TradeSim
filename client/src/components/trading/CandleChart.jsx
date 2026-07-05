// A compact candlestick chart drawn as raw SVG from the sim's candles. No chart library, so it stays
// exact and light. Prices arrive in cents.
function CandleChart({ candles }) {
  const data = candles.slice(-60);

  const width = 600;
  const height = 340;
  const padTop = 10;
  const padBottom = 12;
  const plotHeight = height - padTop - padBottom;

  if (data.length === 0) {
    return (
      <svg className="candle-chart" viewBox={`0 0 ${width} ${height}`} preserveAspectRatio="none">
        <line x1="0" y1={height / 2} x2={width} y2={height / 2} stroke="#1e2430" />
      </svg>
    );
  }

  const highest = Math.max(...data.map((c) => c.highCents));
  const lowest = Math.min(...data.map((c) => c.lowCents));
  const margin = (highest - lowest) * 0.12 || 100;
  const top = highest + margin;
  const bottom = lowest - margin;

  const columnWidth = width / data.length;
  const bodyWidth = Math.max(1, columnWidth * 0.6);
  const yOf = (cents) => padTop + ((top - cents) / (top - bottom)) * plotHeight;

  return (
    <svg className="candle-chart" viewBox={`0 0 ${width} ${height}`} preserveAspectRatio="none">
      {[0.25, 0.5, 0.75].map((fraction) => (
        <line
          key={fraction}
          x1="0"
          y1={padTop + fraction * plotHeight}
          x2={width}
          y2={padTop + fraction * plotHeight}
          stroke="#1e2430"
          strokeWidth="1"
        />
      ))}
      {data.map((candle, index) => {
        const x = index * columnWidth + columnWidth / 2;
        const rising = candle.closeCents >= candle.openCents;
        const color = rising ? '#1ec8a5' : '#f6465d';
        const bodyTop = yOf(Math.max(candle.openCents, candle.closeCents));
        const bodyHeight = Math.max(1, Math.abs(yOf(candle.openCents) - yOf(candle.closeCents)));
        return (
          <g key={index}>
            <line
              x1={x}
              y1={yOf(candle.highCents)}
              x2={x}
              y2={yOf(candle.lowCents)}
              stroke={color}
              strokeWidth="1"
            />
            <rect
              x={x - bodyWidth / 2}
              y={bodyTop}
              width={bodyWidth}
              height={bodyHeight}
              fill={color}
            />
          </g>
        );
      })}
    </svg>
  );
}

export default CandleChart;
