import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import './SearchBar.css';

function SearchBar() {
  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5001';
  const [query, setQuery] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [selectedIndex, setSelectedIndex] = useState(-1);
  const navigate = useNavigate();
  
  const searchInputRef = useRef(null);
  const suggestionsRef = useRef(null);
  const debounceTimeoutRef = useRef(null);

  useEffect(() => {
    function handleClickOutside(event) {
      if (suggestionsRef.current && !suggestionsRef.current.contains(event.target) && 
          searchInputRef.current && !searchInputRef.current.contains(event.target)) {
        setSuggestions([]);
      }
    }

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const fetchSuggestions = async (input) => {
    console.log("Fetching suggestions for:", input);
    if (!input || input.length < 2) {
      setSuggestions([]);
      return;
    }

    try {
      setIsLoading(true);
      setError(null);
      
      const url = `${API_BASE_URL}/paper_trader/security/suggestion/${encodeURIComponent(input)}`;
      console.log("Fetching from URL:", url);
      
      const response = await fetch(url);
      
      if (!response.ok) {
        console.error("API Error Status:", response.status);
        throw new Error('Failed to fetch suggestions');
      }
      
      const data = await response.json();
      console.log("API returned data:", data);
      
      if (Array.isArray(data)) {
        setSuggestions(data);
      } else if (data && typeof data === 'object') {
        const results = data.results || data.suggestions || data.items || [];
        setSuggestions(Array.isArray(results) ? results : []);
      } else {
        setSuggestions([]);
      }
    } catch (err) {
      console.error('Error fetching suggestions:', err);
      setError('Unable to get stock suggestions. Please try again.');
      setSuggestions([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    console.log("Query submitted:", query);
    if (query.trim()) {
      navigate(`/stock/${query.toUpperCase()}`);
      setQuery('');
      setSuggestions([]);
    }
  };

  const handleChange = (e) => {
    console.log("Change event triggered");
    const value = e.target.value;
    setQuery(value);
    setSelectedIndex(-1);
    
    if (debounceTimeoutRef.current) {
      clearTimeout(debounceTimeoutRef.current);
    }
    
    debounceTimeoutRef.current = setTimeout(() => {
      fetchSuggestions(value);
    }, 300);
  };

  const handleKeyDown = (e) => {
    if (!suggestions.length) return;
    
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setSelectedIndex(prev => (prev < suggestions.length - 1 ? prev + 1 : 0));
    }
    else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setSelectedIndex(prev => (prev > 0 ? prev - 1 : suggestions.length - 1));
    }
    else if (e.key === 'Enter' && selectedIndex >= 0) {
      console.log("KLJDFLKJSD" + selected.symbol);
      e.preventDefault();
      const selected = suggestions[selectedIndex];
      navigate(`/stock/${selected.symbol}`);
      setQuery('');
      setSuggestions([]);
    }
    else if (e.key === 'Escape') {
      setSuggestions([]);
      setSelectedIndex(-1);
    }
  };

  const handleSuggestionClick = (symbol) => {
    console.log("Suggestion clicked:", query);
    navigate(`/stock/${query.toUpperCase()}`);
    setQuery('');
    setSuggestions([]);
  };

  return (
    <div className="search-container">
      <form className="search-form" onSubmit={handleSearch}>
        <div className="search-input-container">
          <div className="search-icon-left">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
              <path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001c.03.04.062.078.098.115l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1.007 1.007 0 0 0-.115-.1zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0z"/>
            </svg>
          </div>
          <input
            ref={searchInputRef}
            type="text"
            className="search-input"
            placeholder="Search for stocks or companies..."
            value={query}
            onChange={handleChange}
            onKeyDown={handleKeyDown}
            aria-label="Search stocks"
            autoComplete="off"
          />
          
          {isLoading && (
            <div className="search-spinner"></div>
          )}
          
          {!isLoading && query && (
            <button 
              type="button" 
              className="clear-button"
              onClick={() => {
                setQuery('');
                setSuggestions([]);
                searchInputRef.current.focus();
              }}
              aria-label="Clear search"
            >
              ×
            </button>
          )}
        </div>
        
        {suggestions.length > 0 && (
          <div className="suggestions-container" ref={suggestionsRef}>
            {error && <div className="suggestion-error">{error}</div>}
            {suggestions.map((item, index) => (
              <div 
                key={item.symbol || index} 
                className={`suggestion-item ${index === selectedIndex ? 'selected' : ''}`}
                onClick={() => handleSuggestionClick(item.symbol)}
                onMouseEnter={() => setSelectedIndex(index)}
              >
                <div className="suggestion-symbol">{item.symbol}</div>
                <div className="suggestion-name">{item.name || item.companyName}</div>
                {item.type && <div className="suggestion-type">{item.type}</div>}
              </div>
            ))}
          </div>
        )}
      </form>
    </div>
  );
}

export default SearchBar;