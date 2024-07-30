import React, { createContext, useState, useEffect } from 'react';

// Create a context for the user data
export const UserContext = createContext();

export const UserProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [id, setId] = useState(null);

  useEffect(() => {
    // Check if user data is saved in local storage
    const storedUser = localStorage.getItem('user');
    const storedAccountID = localStorage.getItem('id');
    if (storedUser) {
      setUser(JSON.parse(storedUser));
      setId(JSON.parse(storedAccountID));
    }
  }, []);

  const login = (username, accountID) => {
    setUser({ username });
    setId(accountID);
    localStorage.setItem('user', JSON.stringify({ username }));
    localStorage.setItem('id', JSON.stringify(accountID));
  };

  const logout = (resetForm) => {
    setUser(null);
    setId(null);
    localStorage.removeItem('user');
    localStorage.removeItem('id');
    resetForm();
  };

  return (
    <UserContext.Provider value={{ user, id, login, logout }}>
      {children}
    </UserContext.Provider>
  );
};
