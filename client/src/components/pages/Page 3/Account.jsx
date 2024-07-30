import React, { useState, useContext, useEffect } from 'react';
import { Card, Button, Form, Alert } from 'react-bootstrap';
import 'bootstrap/dist/css/bootstrap.min.css';
import { UserContext } from '../../../UserContext';

export default function Account() {
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    username: "",
    password: "",
    confirmPassword: ""
  });
  const [accountID, setAccountID] = useState(1010110);
  const [error, setError] = useState(null);
  const [showPassword, setShowPassword] = useState(false);
  const [usernameExists, setUsernameExists] = useState(false);
  const { user, id, login, logout } = useContext(UserContext);

  useEffect(() => {
    if (!isLogin && formData.username !== "") {
      checkUsernameExists(formData.username);
    }
  }, [formData.username]);

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
    fetch(`http://localhost:8000/paper_trader/account/get/password?username=${formData.username}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
    })
    .then(response => response.text())
    .then(text => {
        console.log(text)
      try {
        const data = text;
        console.log(data);
        if (data === formData.password) {
          fetch(`http://localhost:8000/paper_trader/account/get/accountID?username=${formData.username}`, {
            method: 'GET',
            headers: {
              'Content-Type': 'application/json'
            },
          })
          .then(response => response.json())
          .then(data => {
            setAccountID(data);
            login(formData.username, data);
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

  const signUpAttempt = () => {
    fetch(`http://localhost:8000/paper_trader/account/add?username=${formData.username}&password=${formData.password}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        accountID,
        firstName: formData.firstName,
        lastName: formData.lastName,
        emailAddress: formData.email,
        password: formData.password
      })
    })
    .then(response => {
      if (response.ok) {
        login(formData.username, accountID);
      } else {
        setError("An error occurred during sign up. Please try again.");
      }
    })
    .catch(error => {
      console.error('Error during sign up:', error);
      setError("An error occurred. Please try again.");
    });
  };

  const checkUsernameExists = (username, callback) => {
    fetch(`http://localhost:8000/paper_trader/account/check/username?username=${username}`, {
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
    setAccountID(1010110);
  };

  if (user) {
    return (
      <div className="d-flex justify-content-center align-items-center vh-100">
        <Card style={{ width: '30rem', padding: '2rem' }}>
          <Card.Body>
            <Card.Title>Welcome, {user.username}</Card.Title>
            <Button variant="primary" onClick={() => logout(resetForm)}>Logout</Button>
          </Card.Body>
        </Card>
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
              {formData.username && (
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
