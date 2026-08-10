// The backend speaks in whole cents and whole shares. These helpers render them for display.

export function priceFromCents(cents) {
  if (cents == null) return '-';
  return (cents / 100).toFixed(2);
}

export function usd(amount) {
  if (amount == null) return '-';
  return amount.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

export function signedUsd(amount) {
  if (amount == null) return '-';
  const sign = amount >= 0 ? '+' : '';
  return sign + usd(amount);
}

// Amounts come from the server as whole cents. Dividing only at the point of display keeps the
// arithmetic on the exact integers for as long as possible.
export function usdFromCents(cents) {
  if (cents == null) return '-';
  return usd(cents / 100);
}

export function signedUsdFromCents(cents) {
  if (cents == null) return '-';
  return signedUsd(cents / 100);
}

export function signedPercent(value) {
  if (value == null || !isFinite(value)) return '-';
  const sign = value >= 0 ? '+' : '';
  return `${sign}${value.toFixed(2)}%`;
}

export function shares(quantity) {
  return quantity.toLocaleString('en-US');
}

export function clockTime(epochMillis) {
  const date = new Date(epochMillis);
  return date.toLocaleTimeString('en-US', { hour12: false });
}

export function shortTime(epochMillis) {
  const date = new Date(epochMillis);
  return date.toLocaleTimeString('en-US', { hour12: false, hour: '2-digit', minute: '2-digit' });
}
