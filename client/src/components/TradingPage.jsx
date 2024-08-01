import React, { useState, useEffect, useContext } from 'react';
import { Col, Button, Container } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { UserContext } from '../UserContext';

function TradingPage({ ticker, currentPrice }) {
    const [activeTab, setActiveTab] = useState('Buy');
    const [orderType, setOrderType] = useState('Buy Order');
    const [buyInOption, setBuyInOption] = useState('Dollars');
    const [quantity, setQuantity] = useState(0);
    const [inputValue, setInputValue] = useState('');
    const [portfolios, setPortfolios] = useState([]);
    const [selectedPortfolioID, setSelectedPortfolioID] = useState('');
    const [buyingPower, setBuyingPower] = useState(null);
    const { user, id } = useContext(UserContext);
    const navigate = useNavigate();

    useEffect(() => {
        if (buyInOption === 'Shares') {
            const shares = parseFloat(inputValue.replace(/,/g, ''));
            setQuantity(shares ? (shares * currentPrice).toFixed(2) : 0);
        } else if (buyInOption === 'Dollars') {
            const dollars = parseFloat(inputValue.replace(/,/g, '').replace('$', ''));
            setQuantity(dollars ? (dollars / currentPrice).toFixed(6) : 0);
        }
    }, [inputValue, buyInOption, currentPrice]);

    useEffect(() => {
        if (id) {
            fetch(`http://localhost:8000/paper_trader/account/get/portfolioList?id=${id}`)
                .then(response => response.json())
                .then(data => {
                    if (Array.isArray(data)) {
                        fetchPortfolios(data);
                    } else {
                        console.error('Error: Invalid data format received for portfolio list');
                    }
                })
                .catch(error => console.error('Error fetching portfolio list:', error));
        }
    }, [id]);

    const fetchPortfolios = (ids) => {
        ids.forEach(portfolioID => {
            fetch(`http://localhost:8000/paper_trader/portfolio/get/name?id=${portfolioID}`)
                .then(res => res.text())
                .then(name => {
                    setPortfolios(prevPortfolios => [...prevPortfolios, { portfolioID, name }]);
                })
                .catch(error => console.error('Error fetching portfolio data:', error));
        });
    };

    const fetchBuyingPower = (portfolioID) => {
        fetch(`http://localhost:8000/paper_trader/portfolio/get/cash?id=${portfolioID}`)
            .then(res => res.json())
            .then(cash => setBuyingPower(cash))
            .catch(error => console.error('Error fetching buying power:', error));
    };

    const handleInputChange = (e) => {
        let value = e.target.value.replace(/[^\d.]/g, ''); // Allow only numbers and dots

        if (value.includes('.')) {
            const parts = value.split('.');
            value = parts[0].slice(0, 7) + '.' + parts[1].slice(0, buyInOption === 'Dollars' ? 2 : 6);
            value = parts.length > 2 ? parts.slice(0, 2).join('.') : value; // Allow only one decimal
        } else {
            value = value.slice(0, buyInOption === 'Dollars' ? 9 : 7);
        }

        value = formatWithCommas(value, buyInOption === 'Dollars' ? 2 : 6);
        if (buyInOption === 'Dollars' && value) {
            value = `$${value}`;
        }
        setInputValue(value);
    };

    const handleOrderTypeChange = (e) => {
        const selectedOrderType = e.target.value;
        if (selectedOrderType === 'Limit Order') {
            alert('This functionality is not currently available on TradeSim.');
            return;
        }
        setOrderType(selectedOrderType);
    };

    const handleTabClick = (tab) => {
        setActiveTab(tab);
        setOrderType(tab === 'Buy' ? 'Buy Order' : 'Sell Order');
        setBuyInOption('Dollars');
        setInputValue('');
        setQuantity(0);
    };

    const handleBuyInOptionChange = (e) => {
        setBuyInOption(e.target.value);
        setInputValue('');
        setQuantity(0);
    };

    const handlePortfolioChange = (e) => {
        const selectedID = e.target.value;
        setSelectedPortfolioID(selectedID);
        fetchBuyingPower(selectedID);
    };

    const formatQuantityWithCommas = (value) => {
        return value ? value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',') : '0';
    };

    if (!user) {
        return (
            <Col md={3} style={{
                position: 'fixed',
                top: '80px',
                right: '10%',
                width: '25%',
                backgroundColor: 'white',
                padding: '20px',
                boxShadow: '0 2px 6px rgba(0,0,0,0.1)',
                maxHeight: 'calc(100vh - 80px)',
                overflowY: 'auto'
            }}>
                <div className="text-center">
                    <h4>Please sign in to trade</h4>
                    <Button variant="primary" onClick={() => navigate("/account", { state: { isLogin: true } })} className="me-2">Login</Button>
                    <Button variant="secondary" onClick={() => navigate("/account", { state: { isLogin: false } })} className="ms-2">Sign Up</Button>
                </div>
            </Col>
        );
    }

    return (
        <Col md={3} style={{
            position: 'fixed',
            top: '80px',
            right: '10%',
            width: '25%',
            backgroundColor: 'white',
            padding: '20px',
            boxShadow: '0 2px 6px rgba(0,0,0,0.1)',
            maxHeight: 'calc(100vh - 80px)',
            overflowY: 'auto'
        }}>
            <div>
                <div style={{ marginBottom: '20px', display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid #000', padding: '0 20px' }}>
                    <span 
                        style={{
                            padding: '15px 20px',
                            borderBottom: activeTab === 'Buy' ? '2px solid #000' : 'none',
                            cursor: 'pointer',
                            flex: '1',
                            textAlign: 'center',
                            fontSize: '16px',
                            fontWeight: 'bold'
                        }}
                        onClick={() => handleTabClick('Buy')}
                    >
                        Buy {ticker}
                    </span>
                    <span
                        style={{
                            padding: '15px 20px',
                            borderBottom: activeTab === 'Sell' ? '2px solid #000' : 'none',
                            cursor: 'pointer',
                            flex: '1',
                            textAlign: 'center',
                            fontSize: '16px',
                            fontWeight: 'bold'
                        }}
                        onClick={() => handleTabClick('Sell')}
                    >
                        Sell {ticker}
                    </span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px', padding: '0 20px' }}>
                    <p style={{ marginRight: '10px', marginBottom: '0', fontSize: '16px' }}>Order Type</p>
                    <select value={orderType} onChange={handleOrderTypeChange} style={{ width: '40%', height: '40px' }}>
                        <option>{activeTab} Order</option>
                        <option>Limit Order</option>
                    </select>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px', padding: '0 20px' }}>
                    <p style={{ marginRight: '10px', marginBottom: '0', fontSize: '16px' }}>Buy In</p>
                    <select value={buyInOption} onChange={handleBuyInOptionChange} style={{ width: '40%', height: '40px' }}>
                        <option>Dollars</option>
                        <option>Shares</option>
                    </select>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px', padding: '0 20px' }}>
                    <p style={{ marginRight: '10px', marginBottom: '0', fontSize: '16px' }}>{buyInOption === 'Dollars' ? 'Amount' : 'Shares'}</p>
                    <input 
                        type="text" 
                        placeholder={buyInOption === 'Dollars' ? '$0.00' : '0'} 
                        style={{ width: '60%', height: '40px' }} 
                        value={inputValue}
                        onChange={handleInputChange}
                        onFocus={() => buyInOption === 'Dollars' && !inputValue && setInputValue('')}
                    />
                </div>
                {buyInOption === 'Shares' && currentPrice !== null && (
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px', padding: '0 20px' }}>
                        <p style={{ marginBottom: '0', fontSize: '16px' }}>Market Price:</p>
                        <p style={{ marginBottom: '0', fontSize: '16px' }}>${currentPrice.toFixed(2)}</p>
                    </div>
                )}
                <div style={{ padding: '0 20px', borderTop: '1px solid #000', marginTop: '20px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', paddingTop: '20px' }}>
                        <p style={{ marginBottom: '0', fontSize: '16px', fontWeight: 'bold' }}>{buyInOption !== 'Dollars' ? 'Est. Cost:' : 'Est. Quantity:'}</p>
                        <p style={{ marginBottom: '0', fontSize: '16px', textAlign: 'right', fontWeight: 'bold' }}>{buyInOption !== 'Dollars' ? `$${formatQuantityWithCommas(quantity)}` : formatQuantityWithCommas(quantity)}</p>
                    </div>
                    <button 
                        style={{ backgroundColor: activeTab === 'Buy' ? 'green' : 'red', color: 'white', padding: '15px 20px', border: 'none', width: '100%', fontSize: '16px', borderRadius: '30px' }}
                        disabled={!inputValue || inputValue === '$0.00' || inputValue === '0'}
                    >
                        Review Order
                    </button>
                    <div style={{ borderTop: '1px solid #000', marginTop: '20px', paddingTop: '10px', textAlign: 'center' }}>
                        <p style={{ fontSize: '16px', marginBottom: '10px' }}>{activeTab === 'Buy' ? `$${buyingPower} buying power available` : 'Selling available'}</p>
                        <select style={{ width: '100%' }} onChange={handlePortfolioChange} value={selectedPortfolioID}>
                            {portfolios.map(portfolio => (
                                <option key={portfolio.portfolioID} value={portfolio.portfolioID}>{portfolio.name}</option>
                            ))}
                        </select>
                    </div>
                </div>
            </div>
        </Col>
    );
}

export default TradingPage;
