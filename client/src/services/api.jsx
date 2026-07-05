const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5001';

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    ...options,
  });

  const text = await response.text();
  const body = text ? JSON.parse(text) : null;

  if (!response.ok) {
    const message = body?.error || body?.message || `Request failed with status ${response.status}`;
    throw new Error(message);
  }

  return body;
}

export const api = {
  // Anonymous session. The server sets an httpOnly cookie and returns the account and its portfolios.
  getSession: () => request('/tradesim/api/session'),
  resetSession: () => request('/tradesim/api/session/reset', { method: 'POST' }),

  getPortfolio: (portfolioID) => request(`/tradesim/api/portfolio/get?portfolioID=${portfolioID}`),
  createPortfolio: (portfolio) =>
    request('/tradesim/api/portfolio/create', {
      method: 'POST',
      body: JSON.stringify(portfolio),
    }),
  deletePortfolio: (portfolioID) =>
    request(`/tradesim/api/portfolio/delete?portfolioID=${portfolioID}`, { method: 'DELETE' }),

  // Market data. Prices come back in whole cents.
  getInstruments: () => request('/tradesim/api/market/instruments'),
  getQuote: (symbol) => request(`/tradesim/api/market/quote?symbol=${symbol}`),
  getDepth: (symbol) => request(`/tradesim/api/market/depth?symbol=${symbol}`),
  getTrades: (symbol) => request(`/tradesim/api/market/trades?symbol=${symbol}`),
  getCandles: (symbol) => request(`/tradesim/api/market/candles?symbol=${symbol}`),

  // Orders and positions for the current session.
  placeOrder: (order) =>
    request('/tradesim/api/order', { method: 'POST', body: JSON.stringify(order) }),
  cancelOrder: (orderId) => request(`/tradesim/api/order?orderId=${orderId}`, { method: 'DELETE' }),
  getOpenOrders: () => request('/tradesim/api/order/open'),
  getPositions: (portfolioID) =>
    request(`/tradesim/api/order/positions?portfolioID=${portfolioID}`),
};

export default api;
