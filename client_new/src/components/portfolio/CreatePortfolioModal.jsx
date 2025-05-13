import { useState, useEffect, useRef } from 'react';
import './CreatePortfolioModal.css';

function CreatePortfolioModal({ isOpen, onClose, onSubmit, isCreating }) {
  const [portfolioName, setPortfolioName] = useState('');
  const [initialBalance, setInitialBalance] = useState('10000');
  const [errors, setErrors] = useState({});
  const modalRef = useRef(null);
  const inputRef = useRef(null);
  
  useEffect(() => {
    if (isOpen && inputRef.current) {
      setTimeout(() => inputRef.current.focus(), 100);
    }
  }, [isOpen]);
  
  useEffect(() => {
    function handleClickOutside(event) {
      if (modalRef.current && !modalRef.current.contains(event.target)) {
        onClose();
      }
    }
    
    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen, onClose]);
  
  useEffect(() => {
    if (!isOpen) {
      setPortfolioName('');
      setInitialBalance('10000');
      setErrors({});
    }
  }, [isOpen]);

  const validate = () => {
    const newErrors = {};
    
    if (!portfolioName.trim()) {
      newErrors.portfolioName = 'Portfolio name is required';
    }
    
    const balance = parseFloat(initialBalance);
    if (isNaN(balance)) {
      newErrors.initialBalance = 'Please enter a valid number';
    } else if (balance < 1000) {
      newErrors.initialBalance = 'Initial balance must be at least $1,000';
    } else if (balance > 1000000) {
      newErrors.initialBalance = 'Initial balance cannot exceed $1,000,000';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (validate()) {
      onSubmit({
        portfolioName: portfolioName.trim(),
        initialBalance: parseFloat(initialBalance)
      });
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-container" ref={modalRef}>
        <div className="modal-header">
          <h3>Create New Portfolio</h3>
          <button className="modal-close-btn" onClick={onClose} aria-label="Close">
            ×
          </button>
        </div>
        
        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-group">
            <label htmlFor="portfolioName">Portfolio Name</label>
            <input
              type="text"
              id="portfolioName"
              ref={inputRef}
              value={portfolioName}
              onChange={(e) => setPortfolioName(e.target.value)}
              placeholder="My Investment Portfolio"
              disabled={isCreating}
              className={errors.portfolioName ? 'error' : ''}
            />
            {errors.portfolioName && <div className="error-message">{errors.portfolioName}</div>}
          </div>
          
          <div className="form-group">
            <label htmlFor="initialBalance">Initial Balance ($)</label>
            <div className="currency-input-container">
              <div className="currency-symbol">$</div>
              <input
                type="text"
                id="initialBalance"
                value={initialBalance}
                onChange={(e) => setInitialBalance(e.target.value.replace(/[^\d.]/g, ''))}
                placeholder="10000"
                disabled={isCreating}
                className={errors.initialBalance ? 'currency-input error' : 'currency-input'}
              />
            </div>
            {errors.initialBalance && <div className="error-message">{errors.initialBalance}</div>}
            <div className="help-text">Min: $1,000 | Max: $1,000,000</div>
          </div>
          
          <div className="modal-footer">
            <button 
              type="button" 
              className="btn btn-secondary" 
              onClick={onClose}
              disabled={isCreating}
            >
              Cancel
            </button>
            <button 
              type="submit" 
              className="btn btn-primary" 
              disabled={isCreating}
            >
              {isCreating ? 'Creating...' : 'Create Portfolio'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default CreatePortfolioModal;