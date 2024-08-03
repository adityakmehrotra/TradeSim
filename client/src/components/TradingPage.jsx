import React, { useState, useEffect, useContext } from 'react';
import { Col, Button, Modal, Alert, Dropdown, DropdownButton } from 'react-bootstrap';
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
    const [selectedPortfolioName, setSelectedPortfolioName] = useState('Choose a Portfolio');
    const [buyingPower, setBuyingPower] = useState(null);
    const [sharesOwned, setSharesOwned] = useState(null);
    const [showModal, setShowModal] = useState(false);
    const [showAlert, setShowAlert] = useState(false);
    const { user, id } = useContext(UserContext);
    const [ cost, setCost ] = useState(0);
    const navigate = useNavigate();

    useEffect(() => {
        if (buyInOption === 'Shares') {
            const shares = parseFloat(inputValue.replace(/,/g, ''));
            setQuantity(shares);
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
        const fetchPortfolioData = async () => {
            for (const portfolioID of ids) {
                try {
                    const nameResponse = await fetch(`http://localhost:8000/paper_trader/portfolio/get/name?id=${portfolioID}`);
                    const name = await nameResponse.text();

                    const cashResponse = await fetch(`http://localhost:8000/paper_trader/portfolio/get/cash?id=${portfolioID}`);
                    const cash = await cashResponse.text();

                    if (typeof name === 'string' && !isNaN(parseFloat(cash))) {
                        portfoliosData.push({ portfolioID, name, cash: parseFloat(cash) });
                    }
                } catch (error) {
                    console.error(`Error fetching portfolio data for ID ${portfolioID}:`, error);
                }
            }

            setPortfolios(portfoliosData);
        };

        fetchPortfolioData();
    };

    const fetchBuyingPower = (portfolioID) => {
        fetch(`http://localhost:8000/paper_trader/portfolio/get/cash?id=${portfolioID}`)
            .then(res => res.json())
            .then(cash => setBuyingPower(cash))
            .catch(error => console.error('Error fetching buying power:', error));
    };

    const fetchSharesOwned = (portfolioID, code) => {
        fetch(`http://localhost:8000/paper_trader/portfolio/get/assetsMap/shares?id=${portfolioID}&code=${code}`)
            .then(res => res.json())
            .then(shares => setSharesOwned(shares))
            .catch(error => console.error('Error fetching shares owned:', error));
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
        setShowAlert(false);
    };

    const handleTabClick = (tab) => {
        setActiveTab(tab);
        setOrderType(tab === 'Buy' ? 'Buy Order' : 'Sell Order');
        setBuyInOption('Dollars');
        setInputValue('');
        setQuantity(0);
        setShowAlert(false);

        if (tab === 'Sell' && selectedPortfolioID) {
            fetchSharesOwned(selectedPortfolioID, ticker);
        }
    };

    const handleBuyInOptionChange = (e) => {
        setBuyInOption(e.target.value);
        setInputValue('');
        setQuantity(0);
        setShowAlert(false);
    };

    const handlePortfolioChange = (portfolioID, name) => {
        setSelectedPortfolioID(portfolioID);
        setSelectedPortfolioName(name);
        fetchBuyingPower(portfolioID);

        if (activeTab === 'Sell') {
            fetchSharesOwned(portfolioID, ticker);
        }
        setShowAlert(false);
    };

    const handleReviewOrderClick = () => {
        if (!selectedPortfolioID) {
            setShowAlert(true);
            return;
        }

        const amount = parseFloat(inputValue.replace(/[,$]/g, ''));
        if (activeTab === 'Buy' && amount > buyingPower) {
            setShowAlert(true);
        } else if (activeTab === 'Sell' && buyInOption === 'Shares' && amount > sharesOwned) {
            setShowAlert(true);
        } else if (activeTab === 'Sell' && buyInOption === 'Dollars' && parseFloat(quantity) > sharesOwned) {
            setShowAlert(true);
        } else {
            setShowAlert(false);
            setCost(quantity * currentPrice);
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
                    shareAmount: buyInOption === 'Shares' ? parseFloat(inputValue.replace(/[,$]/g, '')) : parseFloat(quantity),
                    cashAmount: buyInOption === 'Dollars' ? parseFloat(inputValue.replace(/[,$]/g, '')) : parseFloat((quantity * currentPrice).toFixed(2))
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
                        fetchBuyingPower(selectedPortfolioID);
                        fetchSharesOwned(selectedPortfolioID, ticker);
                    })
                    .catch(error => console.error('Error adding transaction to portfolio:', error));
                })
                .catch(error => console.error('Error creating transaction:', error));
            })
            .catch(error => console.error('Error fetching next transaction ID:', error));
    };

    const formatNumberWithCommas = (number, decimals = 2) => {
        return number.toLocaleString('en-US', { minimumFractionDigits: decimals, maximumFractionDigits: decimals });
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
                            <p style={{ marginBottom: '0', fontSize: '16px', textAlign: 'right', fontWeight: 'bold' }}>{buyInOption === 'Dollars' ? formatNumberWithCommas(parseFloat(quantity) || 0, 6) : `$${formatNumberWithCommas(quantity ? (quantity * currentPrice) : 0, 2)}` }</p>
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
                                {selectedPortfolioID ? 
                                    (activeTab === 'Buy' ? `You do not have enough cash to buy ${ticker}.` : `You do not have enough shares to sell ${ticker}.`)
                                    : 'Please choose a portfolio first.'
                                }
                            </Alert>
                        )}
                        <div style={{ borderTop: '1px solid #000', marginTop: '20px', paddingTop: '10px', textAlign: 'center' }}>
                            {portfolios.length > 0 ? (
                                <>
                                    {selectedPortfolioID && (
                                        activeTab === 'Buy' ? (
                                            <p style={{ fontSize: '16px', marginBottom: '10px' }}>${buyingPower ? formatNumberWithCommas(buyingPower, 2) : '0.00'} buying power available</p>
                                        ) : (
                                            <p style={{ fontSize: '16px', marginBottom: '10px' }}>{sharesOwned ? formatNumberWithCommas(sharesOwned, 6) : '0'} shares available</p>
                                        )
                                    )}
                                    <DropdownButton id="dropdown-basic-button" title={selectedPortfolioName} drop="down">
                                        {portfolios.map(portfolio => (
                                            <Dropdown.Item 
                                                key={portfolio.portfolioID} 
                                                onClick={() => handlePortfolioChange(portfolio.portfolioID, portfolio.name)}
                                            >
                                                {portfolio.name}
                                            </Dropdown.Item>
                                        ))}
                                    </DropdownButton>
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
                    {activeTab === 'Buy' ? (
                        `You are placing a paper market order to buy ${formatNumberWithCommas(parseFloat(quantity), 6)} shares of ${ticker} based on the market price $${currentPrice?.toFixed(2)}. You will spend approximately $${(buyInOption === 'Dollars') ? formatNumberWithCommas(parseFloat(inputValue.replace(/[,$]/g, '')), 2) : formatNumberWithCommas(parseFloat(cost), 2)}.`
                    ) : (
                        `You are placing a paper market order to sell approximately ${formatNumberWithCommas(parseFloat(quantity), 6)} shares of ${ticker} based on the market price $${currentPrice?.toFixed(2)}. You will receive approximately $${formatNumberWithCommas(quantity ? (quantity * currentPrice) : 0, 2)}.`
                    )}
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
