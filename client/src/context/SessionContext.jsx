import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../services/api';

const SessionContext = createContext(null);

export function SessionProvider({ children }) {
  const [session, setSession] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const refresh = useCallback(async () => {
    try {
      const data = await api.getSession();
      setSession(data);
      setError(null);
    } catch (err) {
      console.error('Failed to load session:', err);
      setError('Could not reach the server.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const reset = useCallback(async () => {
    const data = await api.resetSession();
    setSession(data);
    return data;
  }, []);

  const value = {
    accountId: session?.accountId ?? null,
    portfolios: session?.portfolios ?? [],
    loading,
    error,
    refresh,
    reset,
  };

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>;
}

export function useSession() {
  return useContext(SessionContext);
}
