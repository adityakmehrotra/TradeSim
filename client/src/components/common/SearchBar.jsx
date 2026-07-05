import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
import './SearchBar.css';

function SearchBar() {
  const [query, setQuery] = useState('');
  const [instruments, setInstruments] = useState([]);
  const [suggestions, setSuggestions] = useState([]);
  const [selectedIndex, setSelectedIndex] = useState(-1);
  const navigate = useNavigate();

  const searchInputRef = useRef(null);
  const suggestionsRef = useRef(null);

  useEffect(() => {
    api
      .getInstruments()
      .then(setInstruments)
      .catch(() => setInstruments([]));
  }, []);

  useEffect(() => {
    function handleClickOutside(event) {
      if (
        suggestionsRef.current &&
        !suggestionsRef.current.contains(event.target) &&
        searchInputRef.current &&
        !searchInputRef.current.contains(event.target)
      ) {
        setSuggestions([]);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const updateSuggestions = (value) => {
    const term = value.trim().toLowerCase();
    if (!term) {
      setSuggestions([]);
      return;
    }
    setSuggestions(
      instruments
        .filter(
          (instrument) =>
            instrument.symbol.toLowerCase().includes(term) ||
            instrument.name.toLowerCase().includes(term)
        )
        .slice(0, 6)
    );
  };

  const handleChange = (event) => {
    const value = event.target.value;
    setQuery(value);
    setSelectedIndex(-1);
    updateSuggestions(value);
  };

  const go = (symbol) => {
    navigate(`/stock/${symbol}`);
    setQuery('');
    setSuggestions([]);
  };

  const handleSearch = (event) => {
    event.preventDefault();
    const match = suggestions[selectedIndex] || suggestions[0];
    if (match) {
      go(match.symbol);
    }
  };

  const handleKeyDown = (event) => {
    if (!suggestions.length) return;
    if (event.key === 'ArrowDown') {
      event.preventDefault();
      setSelectedIndex((prev) => (prev < suggestions.length - 1 ? prev + 1 : 0));
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      setSelectedIndex((prev) => (prev > 0 ? prev - 1 : suggestions.length - 1));
    } else if (event.key === 'Escape') {
      setSuggestions([]);
      setSelectedIndex(-1);
    }
  };

  return (
    <div className="search-container">
      <form className="search-form" onSubmit={handleSearch}>
        <div className="search-input-container">
          <div className="search-icon-left">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="16"
              height="16"
              fill="currentColor"
              viewBox="0 0 16 16"
            >
              <path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001c.03.04.062.078.098.115l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1.007 1.007 0 0 0-.115-.1zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0z" />
            </svg>
          </div>
          <input
            ref={searchInputRef}
            type="text"
            className="search-input"
            placeholder="Search symbols..."
            value={query}
            onChange={handleChange}
            onKeyDown={handleKeyDown}
            aria-label="Search symbols"
            autoComplete="off"
          />
        </div>

        {suggestions.length > 0 && (
          <div className="suggestions-container" ref={suggestionsRef}>
            {suggestions.map((instrument, index) => (
              <div
                key={instrument.symbol}
                className={`suggestion-item ${index === selectedIndex ? 'selected' : ''}`}
                onClick={() => go(instrument.symbol)}
                onMouseEnter={() => setSelectedIndex(index)}
              >
                <div className="suggestion-symbol">{instrument.symbol}</div>
                <div className="suggestion-name">{instrument.name}</div>
              </div>
            ))}
          </div>
        )}
      </form>
    </div>
  );
}

export default SearchBar;
