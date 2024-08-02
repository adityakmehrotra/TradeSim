import React, { useState, useEffect, useContext } from 'react';
import { Col, Button, Modal, Alert } from 'react-bootstrap';
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
    const [showModal, setShowModal] = useState(false);
    const [showAlert, setShowAlert] = useState(false);
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
        const portfoliosData = [];
        ids.forEach(portfolioID => {
            fetch(`http://localhost:8000/paper_trader/portfolio/get/name?id=${portfolioID}`)
                .then(res => res.text())
                .then(name => {
                    portfoliosData.push({ portfolioID, name });
                    if (portfoliosData.length === ids.length) {
                        setPortfolios(portfoliosData);
                        if (portfoliosData.length > 0) {
                            const firstPortfolioID = portfoliosData[0]?.portfolioID;
                            setSelectedPortfolioID(firstPortfolioID);
                            fetchBuyingPower(firstPortfolioID);
                        } else {
                            setSelectedPortfolioID('');
                            setBuyingPower(null);
                        }
                    }
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
        setShowAlert(false);
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

    const handleReviewOrderClick = () => {
        const amount = parseFloat(inputValue.replace(/[,$]/g, ''));
        if (amount > buyingPower) {
            setShowAlert(true);
        } else {
            setShowAlert(false);
            setShowModal(true);
        }
    };

    const handleCloseModal = () => {
        setShowModal(false);
    };

    const handleConfirmOrder = () => {
        fetch("http://localhost:8000/paper_trader/transaction/get/nextTransactionID")
            .then(response => response.json())
            .then(transactionID => {
                const orderDetails = {
                    transactionID,
                    portfolioID: selectedPortfolioID,
                    accountID: id,
                    orderType: activeTab,
                    securityCode: ticker.toUpperCase(),
                    shareAmount: parseFloat(quantity),
                    cashAmount: parseFloat(inputValue.replace(/[,$]/g, ''))
                };

                fetch("http://localhost:8000/paper_trader/transaction/create", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(orderDetails)
                })
                .then(response => response.json())
                .then(() => {
                    fetch(`http://localhost:8000/paper_trader/portfolio/add/transaction?id=${selectedPortfolioID}&transactionID=${transactionID}`, {
                        method: "POST"
                    })
                    .then(() => {
                        setInputValue('');
                        setQuantity(0);
                        setShowModal(false);
                    })
                    .catch(error => console.error('Error adding transaction to portfolio:', error));
                })
                .catch(error => console.error('Error creating transaction:', error));
            })
            .catch(error => console.error('Error fetching next transaction ID:', error));
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
        <>
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
                            <p style={{ marginBottom: '0', fontSize: '16px' }}>${currentPrice?.toFixed(2)}</p>
                        </div>
                    )}
                    <div style={{ padding: '0 20px', borderTop: '1px solid #000', marginTop: '20px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', paddingTop: '20px' }}>
                            <p style={{ marginBottom: '0', fontSize: '16px', fontWeight: 'bold' }}>{buyInOption !== 'Dollars' ? 'Est. Cost:' : 'Est. Quantity:'}</p>
                            <p style={{ marginBottom: '0', fontSize: '16px', textAlign: 'right', fontWeight: 'bold' }}>{buyInOption !== 'Dollars' ? `$${quantity}` : quantity}</p>
                        </div>
                        <button 
                            style={{ backgroundColor: activeTab === 'Buy' ? 'green' : 'red', color: 'white', padding: '15px 20px', border: 'none', width: '100%', fontSize: '16px', borderRadius: '30px' }}
                            disabled={!inputValue || inputValue === '$0.00' || inputValue === '0'}
                            onClick={handleReviewOrderClick}
                        >
                            Review Order
                        </button>
                        {showAlert && (
                            <Alert variant="danger" onClose={() => setShowAlert(false)} dismissible style={{ marginTop: '10px', marginBottom: '10px' }}>
                                You do not have enough cash to Buy {ticker}.
                            </Alert>
                        )}
                        <div style={{ borderTop: '1px solid #000', marginTop: '20px', paddingTop: '10px', textAlign: 'center' }}>
                            {portfolios.length > 0 ? (
                                <>
                                    <p style={{ fontSize: '16px', marginBottom: '10px' }}>${buyingPower ? buyingPower.toFixed(2) : '0.00'} buying power available</p>
                                    <select style={{ width: '100%' }} onChange={handlePortfolioChange} value={selectedPortfolioID}>
                                        {portfolios.map(portfolio => (
                                            <option key={portfolio.portfolioID} value={portfolio.portfolioID}>{portfolio.name}</option>
                                        ))}
                                    </select>
                                </>
                            ) : (
                                <Button variant="primary" onClick={() => navigate("/create-portfolio")}>Create a new portfolio</Button>
                            )}
                        </div>
                    </div>
                </div>
            </Col>

            <Modal show={showModal} onHide={handleCloseModal}>
                <Modal.Header closeButton>
                    <Modal.Title>Order Summary</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    You are placing a paper market order to {activeTab === 'Buy' ? 'buy' : 'sell'} {ticker} based on the market price ${currentPrice?.toFixed(2)}. You will receive approximately {quantity} shares.
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={handleCloseModal}>
                        Edit
                    </Button>
                    <Button variant="primary" onClick={handleConfirmOrder}>
                        Confirm
                    </Button>
                </Modal.Footer>
            </Modal>
        </>
    );
}

export default TradingPage;
