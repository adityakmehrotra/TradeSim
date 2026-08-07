import { priceFromCents, shortTime } from '../../lib/format';

// Candlestick chart with a volume pane, drawn as raw SVG from the sim's candles. No chart library,
// so the rendering stays exact and light. Prices arrive in cents.
const WIDTH = 860;
const HEIGHT = 420;
const PLOT_RIGHT = 792;
const PRICE_TOP = 10;
const PRICE_BOTTOM = 294;
const VOLUME_TOP = 312;
const VOLUME_BOTTOM = 398;
const TIME_Y = 414;
const VISIBLE = 120;

function CandleChart({ candles, lastCents }) {
  const data = candles.slice(-VISIBLE);

  if (data.length === 0) {
    return (
      <svg className="candle-chart" viewBox={`0 0 ${WIDTH} ${HEIGHT}`}>
        <line x1="0" y1={HEIGHT / 2} x2={WIDTH} y2={HEIGHT / 2} stroke="var(--border)" />
      </svg>
    );
  }

  const last = lastCents ?? data[data.length - 1].closeCents;
  let top = Math.max(last, ...data.map((c) => c.highCents));
  let bottom = Math.min(last, ...data.map((c) => c.lowCents));
  const pad = Math.max(2, Math.round((top - bottom) * 0.08));
  top += pad;
  bottom = Math.max(1, bottom - pad);

  const priceY = (cents) =>
    PRICE_TOP + ((top - cents) / (top - bottom)) * (PRICE_BOTTOM - PRICE_TOP);

  const maxVolume = Math.max(1, ...data.map((c) => c.volume));
  const volumeHeight = (volume) => (volume / maxVolume) * (VOLUME_BOTTOM - VOLUME_TOP);

  const columnWidth = PLOT_RIGHT / data.length;
  const bodyWidth = Math.max(1.5, columnWidth * 0.62);
  const xOf = (index) => index * columnWidth + columnWidth / 2;

  const gridPrices = [0, 1, 2, 3, 4].map((i) => bottom + ((top - bottom) * i) / 4);
  const lastY = priceY(last);
  const rising = last >= data[data.length - 1].openCents;

  const timeMarks = [0, Math.floor(data.length / 2), data.length - 1];

  return (
    <svg className="candle-chart" viewBox={`0 0 ${WIDTH} ${HEIGHT}`}>
      {gridPrices.map((price) => (
        <g key={price}>
          <line
            x1="0"
            y1={priceY(price)}
            x2={PLOT_RIGHT}
            y2={priceY(price)}
            stroke="var(--border)"
            strokeWidth="1"
          />
          <text x={PLOT_RIGHT + 8} y={priceY(price) + 3.5} fontSize="11" fill="var(--muted)">
            {priceFromCents(Math.round(price))}
          </text>
        </g>
      ))}

      {data.map((candle, index) => {
        const x = xOf(index);
        const up = candle.closeCents >= candle.openCents;
        const color = up ? 'var(--up)' : 'var(--down)';
        const bodyTop = priceY(Math.max(candle.openCents, candle.closeCents));
        const bodyHeight = Math.max(
          1,
          Math.abs(priceY(candle.openCents) - priceY(candle.closeCents))
        );
        return (
          <g key={candle.epochMillis}>
            <line
              x1={x}
              y1={priceY(candle.highCents)}
              x2={x}
              y2={priceY(candle.lowCents)}
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
            <rect
              x={x - bodyWidth / 2}
              y={VOLUME_BOTTOM - volumeHeight(candle.volume)}
              width={bodyWidth}
              height={volumeHeight(candle.volume)}
              fill={color}
              opacity="0.45"
            />
          </g>
        );
      })}

      {lastY > PRICE_TOP && lastY < PRICE_BOTTOM && (
        <g>
          <line
            x1="0"
            y1={lastY}
            x2={PLOT_RIGHT}
            y2={lastY}
            stroke={rising ? 'var(--up)' : 'var(--down)'}
            strokeWidth="1"
            strokeDasharray="4 4"
            opacity="0.9"
          />
          <rect
            x={PLOT_RIGHT + 2}
            y={lastY - 9}
            width={62}
            height={18}
            rx="3"
            fill={rising ? 'var(--up)' : 'var(--down)'}
          />
          <text
            x={PLOT_RIGHT + 33}
            y={lastY + 3.5}
            fontSize="11"
            fontWeight="700"
            fill="#0b0e14"
            textAnchor="middle"
          >
            {priceFromCents(last)}
          </text>
        </g>
      )}

      <line
        x1="0"
        y1={VOLUME_BOTTOM + 0.5}
        x2={PLOT_RIGHT}
        y2={VOLUME_BOTTOM + 0.5}
        stroke="var(--border)"
      />

      {timeMarks.map((index, i) => (
        <text
          key={index}
          x={xOf(index)}
          y={TIME_Y}
          fontSize="10"
          fill="var(--muted)"
          textAnchor={i === 0 ? 'start' : i === timeMarks.length - 1 ? 'end' : 'middle'}
        >
          {shortTime(data[index].epochMillis)}
        </text>
      ))}
    </svg>
  );
}

export default CandleChart;
