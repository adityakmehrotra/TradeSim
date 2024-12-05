import { useEffect, useState, useContext } from "react";
import { Card, Button, Container, Row, Col, Form, Modal } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { UserContext } from "../../../UserContext";
import 'bootstrap/dist/css/bootstrap.min.css';

export default function PortfolioList({ setActiveTab }) {
  const [portfolioIDs, setPortfolioIDs] = useState([]);
  const [portfolios, setPortfolios] = useState([]);
  const [newPortfolioName, setNewPortfolioName] = useState("");
  const [newPortfolioCash, setNewPortfolioCash] = useState("");
  const [visiblePortfolios, setVisiblePortfolios] = useState(3);
  const [totalMarketValue, setTotalMarketValue] = useState(0);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [portfolioToDelete, setPortfolioToDelete] = useState(null);
  const { user, id } = useContext(UserContext);
  const navigate = useNavigate();

  useEffect(() => {
    if (id) {
      fetchPortfolioList();
    }

    setActiveTab("Portfolio | TradeSim");
  }, [id, setActiveTab]);

  const fetchPortfolioList = () => {
    fetch(`https://parcel-sides-effort-italiano.trycloudflare.com/paper_trader/account/get/portfolioList?id=${id}`)
      .then(response => response.json())
      .then(data => {
        if (Array.isArray(data)) {
          setPortfolioIDs(data);
          fetchPortfolios(data);
        } else {
          console.error('Error: Invalid data format received for portfolio list');
        }
      })
      .catch(error => console.error('Error fetching portfolio list:', error));
  };

  const fetchPortfolios = (ids) => {
    const portfoliosData = [];
    const fetchPortfolioData = async () => {
      for (const portfolioID of ids) {
        try {
          const nameResponse = await fetch(`https://tradesim-api.adityakmehrotra.com/paper_trader/portfolio/get/name?id=${portfolioID}`);
          const name = await nameResponse.text();

          const cashResponse = await fetch(`https://tradesim-api.adityakmehrotra.com/paper_trader/portfolio/get/initcash?id=${portfolioID}`);
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

  const handlePortfolioClick = (portfolioID) => {
    navigate(`/portfolio/${portfolioID}`);
  };

  const formatNumberWithCommas = (number, decimalPlaces) => {
    if (isNaN(number) || number === null) return '0';
    return number.toFixed(decimalPlaces).replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  };

  const handleCreatePortfolio = () => {
    fetch("https://tradesim-api.adityakmehrotra.com/paper_trader/portfolio/get/nextportfolioID")
      .then(response => response.json())
      .then(data => {
        const newPortfolioID = data;
        const newPortfolio = {
          portfolioID: newPortfolioID,
          accountID: id,
          portfolioName: newPortfolioName,
          cashAmount: parseFloat(newPortfolioCash),
          initialBalance: parseFloat(newPortfolioCash)
        };

        fetch("https://tradesim-api.adityakmehrotra.com/paper_trader/portfolio/create", {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify(newPortfolio)
        })
        .then(response => {
          if (response.ok) {
            return response.json();
          } else {
            throw new Error('Error creating portfolio');
          }
        })
        .then(() => {
          fetch(`https://tradesim-api.adityakmehrotra.com/paper_trader/account/add/portfolioList?id=${id}&portfolioID=${newPortfolioID}`, {
            method: "POST"
          })
          .then(() => {
            setPortfolios([...portfolios, { portfolioID: newPortfolioID, name: newPortfolioName, cash: parseFloat(newPortfolioCash) }]);
            createInitialTransaction(newPortfolioID, parseFloat(newPortfolioCash));
            setNewPortfolioName("");
            setNewPortfolioCash("");
            setShowCreateForm(false);
          })
          .catch(error => console.error('Error adding portfolio to account:', error));
        })
        .catch(error => console.error('Error during portfolio creation:', error));
      })
      .catch(error => console.error('Error fetching next portfolio ID:', error));
  };

  const createInitialTransaction = (portfolioID, cashAmount) => {
    fetch("https://tradesim-api.adityakmehrotra.com/paper_trader/transaction/get/nextTransactionID")
      .then(response => response.json())
      .then(transactionID => {
        const orderDetails = {
          transactionID,
          portfolioID,
          accountID: id,
          orderType: "Fund",
          securityCode: "Cash",
          gmtTime: new Date().toUTCString(),
          shareAmount: 1.0,
          cashAmount: cashAmount
        };

        fetch(`https://tradesim-api.adityakmehrotra.com/paper_trader/portfolio/add/holding?id=${portfolioID}&code=Cash`, {
          method: "POST"
        })
        .catch(error => console.error('Error adding cash to portfolio:', error));

        fetch("https://tradesim-api.adityakmehrotra.com/paper_trader/transaction/create", {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify(orderDetails)
        })
        .then(response => response.json())
        .then(() => {
          fetch(`https://tradesim-api.adityakmehrotra.com/paper_trader/portfolio/add/transaction?id=${portfolioID}&transactionID=${transactionID}`, {
            method: "POST"
          })
          .catch(error => console.error('Error adding transaction to portfolio:', error));
        })
        .catch(error => console.error('Error creating transaction:', error));
      })
      .catch(error => console.error('Error fetching next transaction ID:', error));
  };

  const handleShowMore = () => {
    setVisiblePortfolios(prevVisiblePortfolios => prevVisiblePortfolios + 3);
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
        fetch(`https://tradesim-api.adityakmehrotra.com/paper_trader/account/delete/portfolioList?id=${id}&portfolioID=${portfolioToDelete.portfolioID}`, {
          method: "POST"
        })
        .then(() => {
          fetchPortfolioList();
          setShowModal(false);
          setPortfolioToDelete(null);
        })
        .catch(error => console.error('Error deleting portfolio from account:', error));
      } else {
        throw new Error('Error deleting portfolio');
      }
    })
    .catch(error => console.error('Error during portfolio deletion:', error));
  };

  if (!user) {
    return (
      <Container className="d-flex justify-content-center align-items-center vh-100">
        <div className="text-center">
          <h2>Please sign in or sign up to view your portfolios</h2>
          <Button variant="primary" onClick={() => navigate("/account", { state: { isLogin: true } })} className="me-2">Login</Button>
          <Button variant="secondary" onClick={() => navigate("/account", { state: { isLogin: false } })} className="ms-2">Sign Up</Button>
        </div>
      </Container>
    );
  }

  return (
    <Container>
      <Row className="mb-4">
        <Col>
          <h2>Portfolios</h2>
        </Col>
        <Col className="text-end">
          <Button onClick={() => setShowCreateForm(!showCreateForm)}>Add Portfolio</Button>
        </Col>
      </Row>
      {portfolios.slice(0, visiblePortfolios).map(portfolio => (
        <Row key={portfolio.portfolioID} className="mb-4">
          <Col>
            <Card style={{ width: '100%' }}>
              <Card.Body onClick={() => handlePortfolioClick(portfolio.portfolioID)}>
                <Card.Title>{portfolio.name}</Card.Title>
                <Card.Text>
                  Initial Investment: ${formatNumberWithCommas(portfolio.cash, 2)}
                </Card.Text>
                <Button
                  variant="danger"
                  style={{ float: 'right' }}
                  onClick={(e) => {
                    e.stopPropagation();
                    handleDeleteClick(portfolio);
                  }}
                >
                  Delete
                </Button>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      ))}
      {visiblePortfolios < portfolios.length && (
        <Row className="mb-4">
          <Col>
            <Button onClick={handleShowMore}>Show More</Button>
          </Col>
        </Row>
      )}
      {showCreateForm && (
        <div>
          <br />
          <hr />
          <Row className="mt-4 mb-4">
            <Col>
              <h3>Create a New Portfolio</h3>
              <Form>
                <Form.Group>
                  <Form.Label>Portfolio Name</Form.Label>
                  <Form.Control
                    type="text"
                    value={newPortfolioName}
                    onChange={(e) => setNewPortfolioName(e.target.value)}
                    placeholder="Enter portfolio name"
                  />
                </Form.Group>
                <Form.Group>
                  <Form.Label>Cash Amount</Form.Label>
                  <Form.Control
                    type="number"
                    value={newPortfolioCash}
                    onChange={(e) => setNewPortfolioCash(e.target.value)}
                    placeholder="Enter initial cash amount"
                  />
                </Form.Group>
                <Button onClick={handleCreatePortfolio} className="mt-2">Create Portfolio</Button>
              </Form>
            </Col>
          </Row>
        </div>
      )}
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
