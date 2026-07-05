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
