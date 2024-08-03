import React, { useEffect, useState, useContext } from "react";
import { Card, Container, Row, Col, Table, Button, Dropdown, DropdownButton } from "react-bootstrap";
import { useParams, useNavigate } from "react-router-dom";
import { Pie } from 'react-chartjs-2';
import 'bootstrap/dist/css/bootstrap.min.css';
import Chart from 'chart.js/auto';
import { UserContext } from '../../../UserContext';

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

  const fetchAllPortfolios = () => {
    fetch(`http://localhost:8000/paper_trader/account/get/portfolioList?id=${accountID}`)
      .then(response => response.json())
      .then(data => {
        if (Array.isArray(data)) {
          const portfolioPromises = data.map(id =>
            fetch(`http://localhost:8000/paper_trader/portfolio/get/name?id=${id}`)
              .then(response => response.text())
              .then(name => ({ id, name }))
          );
          Promise.all(portfolioPromises)
            .then(portfolios => {
              setAllPortfolios(portfolios);
            })
            .catch(error => console.error('Error fetching portfolio names:', error));
        } else {
          console.error('Error: Invalid data format received for portfolio list');
        }
      })
      .catch(error => console.error('Error fetching portfolio list:', error));
  };

  const fetchPortfolioDetails = (id) => {
    fetch(`http://localhost:8000/paper_trader/portfolio/get?id=${id}`)
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
          currentPrice: 1
        });
      } else {
        return fetch(`http://localhost:8000/paper_trader/polygon/price?ticker=${asset}`)
          .then(response => {
            if (!response.ok) {
              throw new Error(`Error fetching price for ${asset}`);
            }
            return response.json();
          })
          .then(priceData => ({
            name: asset,
            sharesOwned: assets[asset].sharesOwned,
            currentPrice: priceData?.ticker?.day?.c || 0
          }));
      }
    });

    Promise.all(assetPromises)
      .then(assetData => {
        setAssetData(assetData);
        const chartValues = assetData.map(a => a.name === "Cash" ? a.sharesOwned : a.sharesOwned * a.currentPrice);
        const labels = assetData.map(a => a.name);

        // Check if 'Cash' already exists in the labels
        const cashIndex = labels.indexOf('Cash');
        if (cashIndex !== -1) {
          chartValues[cashIndex] += cashAmount;
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
      fetch(`http://localhost:8000/paper_trader/transaction/get?id=${id}`)
        .then(response => response.json())
    );

    Promise.all(transactionPromises)
      .then(transactionData => {
        // Sort transactions by transactionID in descending order
        transactionData.sort((a, b) => b.transactionID - a.transactionID);
        setTransactions(transactionData);
      })
      .catch(error => console.error('Error fetching transactions:', error));
  };

  const handlePortfolioChange = (id) => {
    setSelectedPortfolioID(id);
    navigate(`/portfolio/${id}`);
  };

  if (!portfolio) {
    return <p>Loading...</p>;
  }

  return (
    <Container>
      <Row className="mb-4">
        <Col>
          <Button variant="secondary" onClick={() => navigate(-1)}>Back</Button>
        </Col>
        <Col className="text-end">
          <DropdownButton id="dropdown-basic-button" title="Select Portfolio">
            {allPortfolios.map(portfolio => (
              <Dropdown.Item 
                key={portfolio.id} 
                onClick={() => handlePortfolioChange(portfolio.id)}
              >
                {portfolio.name}
              </Dropdown.Item>
            ))}
          </DropdownButton>
        </Col>
      </Row>
      <Row className="mb-4">
        <Col>
          <h2>{portfolio.portfolioName}</h2>
          <p>Cash Available: ${portfolio.cashAmount ? portfolio.cashAmount.toFixed(2) : '0.00'}</p>
        </Col>
      </Row>
      <Row>
        <Col md={6}>
          {chartData.datasets ? (
            <Pie data={chartData} />
          ) : (
            <p>Loading chart...</p>
          )}
        </Col>
      </Row>
      <Row>
        <Col>
          <h3>Transactions</h3>
          <Table striped bordered hover>
            <thead>
              <tr>
                <th>Transaction</th>
                <th>Order Type</th>
                <th>Security</th>
                <th>Shares</th>
                <th>Cash</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((transaction, index) => (
                <tr key={index}>
                  <td>{transaction.transactionID}</td>
                  <td>{transaction.orderType}</td>
                  <td>{transaction.securityCode}</td>
                  <td>{transaction.shareAmount}</td>
                  <td>${transaction.cashAmount ? transaction.cashAmount.toFixed(2) : 'N/A'}</td>
                </tr>
              ))}
            </tbody>
          </Table>
        </Col>
      </Row>
    </Container>
  );
}
