import React, { useEffect, useState, useContext } from "react";
import { Container, Row, Col, Table, Button, Dropdown, DropdownButton, Modal } from "react-bootstrap";
import { useParams, useNavigate } from "react-router-dom";
import { Pie } from 'react-chartjs-2';
import 'bootstrap/dist/css/bootstrap.min.css';
import Chart from 'chart.js/auto';
import { UserContext } from '../../../UserContext';
import PortfolioChart from './PortfolioChart';

export default function PortfolioDetails() {
  const { portfolioID } = useParams();
  const navigate = useNavigate();
  const [portfolio, setPortfolio] = useState(null);
  const [assetData, setAssetData] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [chartData, setChartData] = useState({});
  const [allPortfolios, setAllPortfolios] = useState([]);
  const [selectedPortfolioID, setSelectedPortfolioID] = useState(portfolioID);
  const { id: accountID } = useContext(UserContext);
  const [showModal, setShowModal] = useState(false);
  const [portfolioToDelete, setPortfolioToDelete] = useState(null);
  const [totalMarketValue, setTotalMarketValue] = useState(0);
  const [currPortfolio, setCurrPortfolio] = useState("Select Portfolio");

  useEffect(() => {
    if (accountID) {
      fetchAllPortfolios();
    }
  
  }, [accountID]);

  useEffect(() => {
    if (selectedPortfolioID) {
      fetchPortfolioDetails(selectedPortfolioID);
    }
  }, [selectedPortfolioID]);

  useEffect(() => {
    const calcTotalVal = () => {
      const totalVal = assetData.reduce((total, asset) => {
        const marketValue = asset.name === "Cash" 
          ? portfolio.cashAmount 
          : asset.sharesOwned * asset.currentPrice;
        return total + marketValue;
      }, 0);

      setTotalMarketValue(totalVal);
    };

    calcTotalVal();
    console.log("Total Market Value:", totalMarketValue);

  }, [assetData, portfolio, totalMarketValue]);

  const fetchAllPortfolios = () => {
    fetch(`https://tradesim-api.adityakmehrotra.com/paper_trader/account/get/portfolioList?id=${accountID}`)
      .then(response => response.json())
      .then(data => {
        if (Array.isArray(data)) {
          const portfolioPromises = data.map(id =>
            fetch(`https://tradesim-api.adityakmehrotra.com/paper_trader/portfolio/get/name?id=${id}`)
              .then(response => {
                if (response.ok) {
                  return response.text().then(name => ({ id, name }));
                } else {
                  return null;
                }
              })
          );
          Promise.all(portfolioPromises)
            .then(portfolios => {
              const validPortfolios = portfolios.filter(p => p !== null);
              setAllPortfolios(validPortfolios);
            })
            .catch(error => console.error('Error fetching portfolio names:', error));
        } else {
          console.error('Error: Invalid data format received for portfolio list');
        }
      })
      .catch(error => console.error('Error fetching portfolio list:', error));
  };

  const fetchPortfolioDetails = (id) => {
    fetch(`https://tradesim-api.adityakmehrotra.com/paper_trader/portfolio/get?id=${id}`)
      .then(response => response.json())
      .then(data => {
        setPortfolio(data);
        if (data.assets) {
          fetchAssetPrices(data.assets, data.cashAmount);
        } else {
          setChartData({
            labels: ['Cash'],
            datasets: [{
              data: [data.cashAmount],
              backgroundColor: ['#FF6384']
            }]
          });
        }
        if (data.transactionList) {
          fetchTransactions(data.transactionList);
        }
      })
      .catch(error => console.error('Error fetching portfolio details:', error));
  };

  const fetchAssetPrices = (assets, cashAmount) => {
    const assetPromises = Object.keys(assets).map(asset => {
      if (asset === "Cash") {
        return Promise.resolve({
          name: asset,
          sharesOwned: assets[asset].sharesOwned,
          initialCashInvestment: assets[asset].initialCashInvestment,
          currentPrice: 1
        });
      } else {
        return fetch(`https://tradesim-api.adityakmehrotra.com/paper_trader/polygon/price?ticker=${asset}`)
          .then(response => {
            if (!response.ok) {
              throw new Error(`Error fetching price for ${asset}`);
            }
            return response.json();
          })
          .then(priceData => ({
            name: asset,
            sharesOwned: assets[asset].sharesOwned,
            initialCashInvestment: assets[asset].initialCashInvestment,
            currentPrice: priceData?.ticker?.day?.c || 0
          }));
      }
    });

    Promise.all(assetPromises)
      .then(assetData => {
        setAssetData(assetData);
        const chartValues = assetData.map(a => a.name === "Cash" ? cashAmount : a.sharesOwned * a.currentPrice);
        const labels = assetData.map(a => a.name);

        const cashIndex = labels.indexOf('Cash');
        if (cashIndex !== -1) {
          chartValues[cashIndex] = cashAmount;
        } else {
          chartValues.push(cashAmount);
          labels.push('Cash');
        }

        setChartData({
          labels,
          datasets: [{
            data: chartValues,
            backgroundColor: [
              '#FF6384',
              '#36A2EB',
              '#FFCE56',
              '#4BC0C0',
              '#9966FF',
              '#FF9F40',
              '#AA6384'
            ]
          }]
        });
      })
      .catch(error => console.error('Error fetching asset prices:', error));
  };

  const fetchTransactions = (transactionIDs) => {
    const transactionPromises = transactionIDs.map(id =>
      fetch(`https://tradesim-api.adityakmehrotra.com/paper_trader/transaction/get?id=${id}`)
        .then(response => response.json())
    );

    Promise.all(transactionPromises)
      .then(transactionData => {
        transactionData.sort((a, b) => a.transactionID - b.transactionID);
        setTransactions(transactionData);
      })
      .catch(error => console.error('Error fetching transactions:', error));
  };

  const handlePortfolioChange = (portfolio) => {
    setSelectedPortfolioID(portfolio.id);
    setCurrPortfolio(portfolio.name);
    navigate(`/portfolio/${portfolio.id}`);
  };

  const handleDeleteClick = (portfolio) => {
    setPortfolioToDelete(portfolio);
    setShowModal(true);
  };

  const handleConfirmDelete = () => {
    fetch(`https://tradesim-api.adityakmehrotra.com/paper_trader/portfolio/remove?id=${portfolioToDelete.portfolioID}`, {
      method: "DELETE"
    })
    .then(response => {
      if (response.ok) {
        return fetch(`https://tradesim-api.adityakmehrotra.com/paper_trader/account/delete/portfolioList?id=${accountID}&portfolioID=${portfolioToDelete.portfolioID}`, {
          method: "DELETE"
        });
      } else {
        throw new Error('Error deleting portfolio');
      }
    })
    .then(response => {
      if (response.ok) {
        fetchAllPortfolios();
        setShowModal(false);
        setPortfolioToDelete(null);
      } else {
        throw new Error('Error updating account portfolio list');
      }
    })
    .catch(error => console.error('Error during portfolio deletion:', error));
  };

  const formatNumberWithCommas = (number, decimalPlaces) => {
    if (isNaN(number) || number === null) return '0';
    return number.toFixed(decimalPlaces).replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  };

  

  const chartOptions = {
    plugins: {
      legend: {
        position: 'right',
        labels: {
          boxWidth: 20,
          padding: 20,
        }
      },
      tooltip: {
        callbacks: {
          label: function(context) {
            let label = context.label || '';
            if (label) {
              label += ': ';
            }
            if (context.parsed !== null) {
              label += new Intl.NumberFormat('en-US', {
                style: 'currency',
                currency: 'USD'
              }).format(context.parsed);
            }
            return label;
          }
        }
      }
    },
    responsive: true,
    maintainAspectRatio: false,
  };

  if (!portfolio) {
    return <p>Loading...</p>;
  }

  return (
    <Container>
      <Row className="mb-4">
        <Col>
          <Button variant="secondary" onClick={() => navigate("/portfolio")}>Back</Button>
        </Col>
        <Col className="text-end">
          <DropdownButton id="dropdown-basic-button" title={currPortfolio === "Select Portfolio" ? portfolio?.portfolioName : currPortfolio}>
            {allPortfolios.map(portfolio => (
              <Dropdown.Item 
                key={portfolio.id} 
                onClick={() => handlePortfolioChange(portfolio)}
              >
                {portfolio.name}
              </Dropdown.Item>
            ))}
          </DropdownButton>
        </Col>
      </Row>
      <Row className="mb-4">
        <Col>
          <h2>{portfolio?.portfolioName}</h2>
          <p>Current Value: ${totalMarketValue.toFixed(2)}</p>
        </Col>
      </Row>
      <Row>
        <Col md={4}>
          {chartData.datasets ? (
            <div style={{ width: '300px', height: '300px', marginBottom: '60px' }}>
              <Pie data={chartData} options={chartOptions} />
              <p>Buying Power: ${portfolio?.cashAmount ? portfolio.cashAmount.toFixed(2) : '0.00'}</p>
            </div>
          ) : (
            <p>Loading chart...</p>
          )}
        </Col>
        {
          /**
           * 
          /**
            <Col md={8}>
              <PortfolioChart portfolioID={selectedPortfolioID} />
            </Col>
          */
        }
      </Row>
      <Row>
        <Col>
          <h3>Assets</h3>
          <Table striped bordered hover>
            <thead>
              <tr>
                <th>Security</th>
                <th>Shares Owned</th>
                <th>Initial Investment</th>
                <th>Current Market Value</th>
              </tr>
            </thead>
            <tbody>
              {assetData.map((asset, index) => (
                <tr key={index}>
                  <td>{asset.name}</td>
                  <td>{asset.name === "Cash" ? '' : asset.sharesOwned.toFixed(4)}</td>
                  <td>${formatNumberWithCommas(asset.initialCashInvestment, 2)}</td>
                  <td>${formatNumberWithCommas(asset.name === "Cash" ? portfolio.cashAmount : asset.sharesOwned * asset.currentPrice, 2)}</td>
                </tr>
              ))}
            </tbody>
          </Table>
        </Col>
      </Row>
      <Row style={{ marginTop: '40px' }}>
        <Col>
          <h3>Transactions</h3>
          <Table striped bordered hover>
            <thead>
              <tr>
                <th>#</th>
                <th>Order Type</th>
                <th>Security</th>
                <th>Shares</th>
                <th>Cash</th>
              </tr>
            </thead>
            <tbody>
              {transactions.slice().reverse().map((transaction, index) => (
                <tr key={index}>
                  <td>{transactions.length - index}</td>
                  <td>{transaction.orderType}</td>
                  <td>{transaction.securityCode}</td>
                  <td>{transaction.securityCode === "Cash" ? '' : transaction.shareAmount.toFixed(4)}</td>
                  <td>${transaction.cashAmount ? formatNumberWithCommas(transaction.cashAmount, 2) : 'N/A'}</td>
                </tr>
              ))}
            </tbody>
          </Table>
        </Col>
      </Row>
  
      <Modal show={showModal} onHide={() => setShowModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>Confirm Deletion</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          Are you sure you want to delete {portfolioToDelete?.name}?
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowModal(false)}>
            Cancel
          </Button>
          <Button variant="danger" onClick={handleConfirmDelete}>
            Confirm
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );  
}
