import React, { useState, useContext, useEffect } from 'react';
import { Card, Button, Form, Alert, Modal } from 'react-bootstrap';
import { useLocation, useNavigate } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import { UserContext } from '../../../UserContext';

export default function Account() {
  const location = useLocation();
  const navigate = useNavigate();
  const [isLogin, setIsLogin] = useState(location.state?.isLogin ?? true);
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    username: "",
    password: "",
    confirmPassword: ""
  });
  const [error, setError] = useState(null);
  const [showPassword, setShowPassword] = useState(false);
  const [usernameExists, setUsernameExists] = useState(false);
  const { user, id, login, logout } = useContext(UserContext);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  useEffect(() => {
    if (!isLogin && formData.username !== "") {
      checkUsernameExists(formData.username);
    }
  }, [formData.username, isLogin]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    setError(null);
    if (isLogin) {
      loginAttempt();
    } else {
      if (formData.password !== formData.confirmPassword) {
        setError("Passwords do not match!");
        return;
      }
      checkUsernameExists(formData.username, signUpAttempt);
    }
  };

  const toggleForm = () => {
    setIsLogin(!isLogin);
    resetForm();
    setError(null);
  };

  const loginAttempt = () => {
    fetch(`https://tradesim-api.adityakmehrotra.com/paper_trader/account/get/password?username=${formData.username}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
    })
    .then(response => response.text())
    .then(text => {
      try {
        const data = text;
        if (data === formData.password) {
          fetch(`https://tradesim-api.adityakmehrotra.com/paper_trader/account/get/accountID?username=${formData.username}`, {
            method: 'GET',
            headers: {
              'Content-Type': 'application/json'
            },
          })
          .then(response => response.json())
          .then(data => {
            login(formData.username, data); // Set the logged-in id in UserContext
          })
          .catch(error => {
            console.error('Error fetching account ID:', error);
            setError("An error occurred. Please try again.");
          });
        } else {
          setError("This username and/or password is incorrect");
        }
      } catch (error) {
        setError("Error");
      }
    })
    .catch(error => {
      console.error('Error fetching password:', error);
      setError("An error occurred. Please try again.");
    });
  };

  const signUpAttempt = async () => {
    try {
      const response = await fetch(`https://tradesim-api.adityakmehrotra.com/paper_trader/account/get/nextAccountID`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        },
      });
      const newAccountID = await response.json();

      await fetch(`https://tradesim-api.adityakmehrotra.com/paper_trader/account/add?username=${formData.username}&password=${formData.password}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          accountID: newAccountID,
          firstName: formData.firstName,
          lastName: formData.lastName,
          emailAddress: formData.email,
          password: formData.password
        })
      });

      login(formData.username, newAccountID); // Set the logged-in id in UserContext
    } catch (error) {
      console.error('Error during sign up:', error);
      setError("An error occurred. Please try again.");
    }
  };

  const checkUsernameExists = (username, callback) => {
    fetch(`https://tradesim-api.adityakmehrotra.com/paper_trader/account/check/username?username=${username}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
    })
    .then(response => response.text())
    .then(text => {
      const exists = text === 'true';
      setUsernameExists(exists);
      if (callback && !exists) {
        callback();
      }
    })
    .catch(error => {
      console.error('Error checking username:', error);
      setError("An error occurred. Please try again.");
    });
  };

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  const resetForm = () => {
    setFormData({
      firstName: "",
      lastName: "",
      email: "",
      username: "",
      password: "",
      confirmPassword: ""
    });
    setError(null);
  };

  const handleDeleteAccount = () => {
    fetch(`https://tradesim-api.adityakmehrotra.com/paper_trader/account/delete?id=${id}`, {
      method: 'DELETE'
    })
    .then(response => {
      if (response.ok) {
        logout(resetForm);
        setShowDeleteModal(false);
        navigate("/account", { state: { isLogin: true } });
      } else {
        throw new Error('Error deleting account');
      }
    })
    .catch(error => {
      console.error('Error during account deletion:', error);
      setError("An error occurred. Please try again.");
    });
  };

  if (user) {
    return (
      <div className="d-flex justify-content-center align-items-center vh-100">
        <Card style={{ width: '30rem', padding: '2rem' }}>
          <Card.Body>
            <Card.Title>Welcome, {user.username}</Card.Title>
            <div className="mb-3"></div>
            <div className="d-flex justify-content-between">
              <Button variant="primary" onClick={() => logout(resetForm)}>Logout</Button>
              <Button variant="danger" onClick={() => setShowDeleteModal(true)}>Delete Account</Button>
            </div>
          </Card.Body>
        </Card>
        <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)}>
          <Modal.Header closeButton>
            <Modal.Title>Confirm Account Deletion</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            Are you sure you want to delete your account? This action cannot be undone.
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowDeleteModal(false)}>
              Cancel
            </Button>
            <Button variant="danger" onClick={handleDeleteAccount}>
              Delete
            </Button>
          </Modal.Footer>
        </Modal>
      </div>
    );
  }

  return (
    <div className="d-flex justify-content-center align-items-center vh-100">
      <Card style={{ width: '30rem', padding: '2rem' }}>
        <Card.Body>
          <Card.Title>{isLogin ? "Login" : "Sign Up"}</Card.Title>
          {error && <Alert variant="danger">{error}</Alert>}
          <Form onSubmit={handleSubmit}>
            {!isLogin && (
              <>
                <Form.Group controlId="formFirstName">
                  <Form.Label>First Name</Form.Label>
                  <Form.Control
                    type="text"
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleInputChange}
                    placeholder="Enter first name"
                    required
                  />
                </Form.Group>
                <Form.Group controlId="formLastName">
                  <Form.Label>Last Name</Form.Label>
                  <Form.Control
                    type="text"
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleInputChange}
                    placeholder="Enter last name"
                    required
                  />
                </Form.Group>
                <Form.Group controlId="formEmail">
                  <Form.Label>Email address</Form.Label>
                  <Form.Control
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleInputChange}
                    placeholder="Enter email"
                    required
                  />
                </Form.Group>
              </>
            )}
            <Form.Group controlId="formUsername">
              <Form.Label>Username</Form.Label>
              <Form.Control
                type="text"
                name="username"
                value={formData.username}
                onChange={handleInputChange}
                placeholder="Enter username"
                required
              />
              {formData.username && !isLogin && (
                <Form.Text className={usernameExists ? "text-danger" : "text-success"}>
                  {usernameExists ? "Username already exists. Please pick a different username." : "Username is available."}
                </Form.Text>
              )}
            </Form.Group>
            <Form.Group controlId="formPassword">
              <Form.Label>Password</Form.Label>
              <div className="d-flex">
                <Form.Control
                  type={showPassword ? "text" : "password"}
                  name="password"
                  value={formData.password}
                  onChange={handleInputChange}
                  placeholder="Password"
                  required
                />
                <Button onClick={togglePasswordVisibility} variant="outline-secondary">
                  {showPassword ? "Hide" : "Show"}
                </Button>
              </div>
            </Form.Group>
            {!isLogin && (
              <Form.Group controlId="formConfirmPassword">
                <Form.Label>Confirm Password</Form.Label>
                <Form.Control
                  type="password"
                  name="confirmPassword"
                  value={formData.confirmPassword}
                  onChange={handleInputChange}
                  placeholder="Confirm Password"
                  required
                />
              </Form.Group>
            )}
            <Button variant="primary" type="submit" className="w-100 mt-3">
              {isLogin ? "Login" : "Sign Up"}
            </Button>
          </Form>
          <div className="mt-3">
            <Button variant="link" onClick={toggleForm}>
              {isLogin ? "Don't have an account? Sign Up" : "Already have an account? Login"}
            </Button>
          </div>
        </Card.Body>
      </Card>
    </div>
  );
}
