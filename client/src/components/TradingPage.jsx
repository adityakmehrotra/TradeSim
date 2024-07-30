import React, { useState, useEffect } from 'react';
import { Col } from 'react-bootstrap';

function TradingPage({ ticker, currentPrice }) {
    const [activeTab, setActiveTab] = useState('Buy');
    const [shareOrDollarOption, setShareOrDollarOption] = useState('Buy In');
    const [orderType, setOrderType] = useState('Buy Order');
    const [buyInOption, setBuyInOption] = useState('Dollars');
    const [quantity, setQuantity] = useState(0);
    const [inputValue, setInputValue] = useState('');

    useEffect(() => {
        if (buyInOption === 'Shares') {
            const shares = parseFloat(inputValue.replace(/,/g, ''));
            setQuantity(shares ? (shares * currentPrice).toFixed(2) : 0);
        } else if (buyInOption === 'Dollars') {
            const dollars = parseFloat(inputValue.replace(/,/g, '').replace('$', ''));
            setQuantity(dollars ? (dollars / currentPrice).toFixed(6) : 0);
        }
    }, [inputValue, buyInOption, currentPrice]);

    const formatWithCommas = (value, maxDigitsAfterDecimal) => {
        if (!value) return '';
        const [integer, decimal] = value.split('.');
        const formattedInteger = integer.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
        return decimal ? `${formattedInteger}.${decimal.slice(0, maxDigitsAfterDecimal)}` : formattedInteger;
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

    const formatQuantityWithCommas = (value) => {
        return value ? value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',') : '0';
    };

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
                    <p style={{ marginRight: '10px', marginBottom: '0', fontSize: '16px' }}>{shareOrDollarOption}</p>
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
                        <p style={{ fontSize: '16px', marginBottom: '10px' }}>{activeTab === 'Buy' ? 'Buying' : 'Selling'} available</p>
                        <select style={{ width: '100%' }}>
                            <option>Test 1</option>
                            <option>Test 2</option>
                        </select>
                    </div>
                </div>
            </div>
        </Col>
    );
}

export default TradingPage;
