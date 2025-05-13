import { createContext, useState, useEffect } from 'react';
import api from '../services/api';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      try {
        const userData = JSON.parse(storedUser);
        setUser(userData);
      } catch (err) {
        console.error('Failed to parse stored user data:', err);
        localStorage.removeItem('user');
      }
    }
    setLoading(false);
  }, []);

  const login = async (username, password) => {
    setError(null);
    try {
      const response = await api.login(username, password);
      
      const userData = {
        username: response.username,
        accountId: response.account.accountID,
        name: response.account.name,
        account: response.account,
      };
      
      setUser(userData);
      localStorage.setItem('user', JSON.stringify(userData));
      
      return userData;
    } catch (err) {
      setError(err.message || 'An error occurred during login');
      throw err;
    }
  };

  const signup = async (userData, username, password) => {
    setError(null);
    try {
      const response = await api.signup(userData, username, password);
      
      const newUserData = {
        username: username,
        accountId: response.accountID,
        name: userData.name,
        account: response
      };
      
      setUser(newUserData);
      localStorage.setItem('user', JSON.stringify(newUserData));
      
      return newUserData;
    } catch (err) {
      setError(err.message || 'An error occurred during signup');
      throw err;
    }
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('user');
  };

  return (
    <AuthContext.Provider value={{ 
      user, 
      login, 
      signup,
      logout, 
      loading,
      error,
      setError
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthProvider;