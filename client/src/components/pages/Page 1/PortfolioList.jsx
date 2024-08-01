import { useEffect, useState, useContext } from "react";
import { Card, Button, Container, Row, Col, Form } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { UserContext } from "../../../UserContext";
import 'bootstrap/dist/css/bootstrap.min.css';

export default function PortfolioList() {
  const [portfolioIDs, setPortfolioIDs] = useState([]);
  const [portfolios, setPortfolios] = useState([]);
  const [newPortfolioName, setNewPortfolioName] = useState("");
  const [newPortfolioCash, setNewPortfolioCash] = useState("");
  const { user, id } = useContext(UserContext);
  const navigate = useNavigate();

  useEffect(() => {
    if (id) {
      fetch(`http://localhost:8000/paper_trader/account/get/portfolioList?id=${id}`)
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
    }
  }, [id]);

  const fetchPortfolios = (ids) => {
    ids.forEach(portfolioID => {
      Promise.all([
        fetch(`http://localhost:8000/paper_trader/portfolio/get/name?id=${portfolioID}`).then(res => res.text()),
        fetch(`http://localhost:8000/paper_trader/portfolio/get/cash?id=${portfolioID}`).then(res => res.text())
      ])
      .then(([name, cash]) => {
        if (typeof name === 'string' && !isNaN(parseFloat(cash))) {
          setPortfolios(prevPortfolios => [...prevPortfolios, { portfolioID, name, cash: parseFloat(cash) }]);
        } else {
          console.error('Error: Invalid data format received for portfolio details');
        }
      })
      .catch(error => console.error('Error fetching portfolio data:', error));
    });
  };

  const handlePortfolioClick = (portfolioID) => {
    navigate(`/portfolio/${portfolioID}`);
  };

  const handleCreatePortfolio = () => {
    // Fetch the next portfolio ID from the server
    fetch("http://localhost:8000/paper_trader/portfolio/get/nextportfolioID")
      .then(response => response.json())
      .then(data => {
        const newPortfolioID = data; // Assuming the endpoint directly returns an integer ID
        const newPortfolio = {
          portfolioID: newPortfolioID,
          accountID: id,
          portfolioName: newPortfolioName,
          cashAmount: parseFloat(newPortfolioCash)
        };

        // Create the new portfolio with the fetched ID
        fetch("http://localhost:8000/paper_trader/portfolio/create", {
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
          fetch(`http://localhost:8000/paper_trader/account/add/portfolioList?id=${id}&portfolioID=${newPortfolioID}`, {
            method: "POST"
          })
          .then(() => {
            setPortfolios([...portfolios, { portfolioID: newPortfolioID, name: newPortfolioName, cash: parseFloat(newPortfolioCash) }]);
            setNewPortfolioName("");
            setNewPortfolioCash("");
          })
          .catch(error => console.error('Error adding portfolio to account:', error));
        })
        .catch(error => console.error('Error during portfolio creation:', error));
      })
      .catch(error => console.error('Error fetching next portfolio ID:', error));
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
      </Row>
      <Row>
        {portfolios.map(portfolio => (
          <Card key={portfolio.portfolioID} onClick={() => handlePortfolioClick(portfolio.portfolioID)} className="mb-4" style={{ width: '100%' }}>
            <Card.Body>
              <Card.Title>{portfolio.name}</Card.Title>
              <Card.Text>
                Cash: ${portfolio.cash.toFixed(2)}
              </Card.Text>
            </Card.Body>
          </Card>
        ))}
      </Row>
      <Row className="mt-4">
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
    </Container>
  );
}
