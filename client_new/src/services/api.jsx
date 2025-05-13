const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5001';

export const api = {
  signup: async (userData, username, password) => {
    try {
      const response = await fetch(`${API_BASE_URL}/paper_trader/account/add?username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(userData),
      });
      
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Failed to create account');
      }
      
      return await response.json();
    } catch (error) {
      console.error('Signup error:', error);
      throw error;
    }
  },
  
  login: async (username, password) => {
    try {
      const params = new URLSearchParams();
      params.append('username', username);
      params.append('password', password);
      
      const response = await fetch(`${API_BASE_URL}/paper_trader/account/login?${params.toString()}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
      });
      
      const data = await response.json();
      
      if (!response.ok || !data.success) {
        throw new Error(data.message || 'Login failed');
      }
      
      return data;
    } catch (error) {
      console.error('Login API error:', error);
      throw error;
    }
  },
  
  getAllAccounts: async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/paper_trader/account/all`);
      
      if (!response.ok) {
        throw new Error('Failed to fetch accounts');
      }
      
      return await response.json();
    } catch (error) {
      console.error('Get accounts error:', error);
      throw error;
    }
  },
  
  getAccount: async (id) => {
    try {
      const response = await fetch(`${API_BASE_URL}/paper_trader/account/get?id=${id}`);
      
      if (!response.ok) {
        throw new Error('Failed to fetch account');
      }
      
      return await response.json();
    } catch (error) {
      console.error('Get account error:', error);
      throw error;
    }
  },
  
  deleteAccount: async (id) => {
    try {
      const response = await fetch(`${API_BASE_URL}/paper_trader/account/delete?id=${id}`, {
        method: 'DELETE',
      });
      
      if (!response.ok) {
        throw new Error('Failed to delete account');
      }
      
      return true;
    } catch (error) {
      console.error('Delete account error:', error);
      throw error;
    }
  },

  getAllPortfolios: async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/paper_trader/portfolio/all`);
      
      if (!response.ok) {
        throw new Error('Failed to fetch portfolios');
      }
      
      return await response.json();
    } catch (error) {
      console.error('Get portfolios error:', error);
      throw error;
    }
  },
  
  getPortfolio: async (id) => {
    try {
      const response = await fetch(`${API_BASE_URL}/paper_trader/portfolio/get?id=${id}`);
      
      if (!response.ok) {
        throw new Error('Failed to fetch portfolio');
      }
      
      return await response.json();
    } catch (error) {
      console.error('Get portfolio error:', error);
      throw error;
    }
  },
  
  getPortfolioAssets: async (id) => {
    try {
      const response = await fetch(`${API_BASE_URL}/paper_trader/portfolio/get/assets?id=${id}`);
      
      if (!response.ok) {
        throw new Error('Failed to fetch portfolio assets');
      }
      
      return await response.json();
    } catch (error) {
      console.error('Get portfolio assets error:', error);
      throw error;
    }
  },
  
  createPortfolio: async (portfolioData) => {
    try {
      const response = await fetch(`${API_BASE_URL}/paper_trader/portfolio/create`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(portfolioData),
      });
      
      if (!response.ok) {
        throw new Error('Failed to create portfolio');
      }
      
      return await response.json();
    } catch (error) {
      console.error('Create portfolio error:', error);
      throw error;
    }
  },

  getNextPortfolioId: async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/paper_trader/portfolio/get/nextportfolioID`);
      
      if (!response.ok) {
        throw new Error('Failed to get next portfolio ID');
      }
      
      return await response.json();
    } catch (error) {
      console.error('Get next portfolio ID error:', error);
      throw error;
    }
  },

  deletePortfolio: async (id) => {
    try {
      const response = await fetch(`${API_BASE_URL}/paper_trader/portfolio/remove?id=${id}`, {
        method: 'DELETE',
      });
      
      if (!response.ok) {
        throw new Error('Failed to delete portfolio');
      }
      
      return true;
    } catch (error) {
      console.error('Delete portfolio error:', error);
      throw error;
    }
  },

  createPortfolio: async (portfolioData) => {
    try {
      const response = await fetch(`${API_BASE_URL}/paper_trader/portfolio/create`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(portfolioData),
      });
      
      if (!response.ok) {
        throw new Error('Failed to create portfolio');
      }
      
      return await response.json();
    } catch (error) {
      console.error('Create portfolio error:', error);
      throw error;
    }
  }
};

export default api;