import React, { useState, useEffect, useRef } from "react";
import { Container, Nav, Navbar, Form, FormControl, Button, Dropdown } from "react-bootstrap";
import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import axios from 'axios';

import '../App.css';
import Portfolio from './pages/Page 1/PortfolioList';
import Notifications from './pages/Page 2/Notifications';
import Account from './pages/Page 3/Account';
import SecurityPage from './SecurityPage.jsx';

export default function AppLayout() {
    const [searchText, setSearchText] = useState("");
    const [suggestions, setSuggestions] = useState([]);
    const navigate = useNavigate();
    const searchInputRef = useRef();

    const handleSearch = async (ticker) => {
        try {
            await axios.get(`http://tradesim-api.adityakmehrotra.com/paper_trader/security/suggestion/${ticker}`);
            navigate(`/search/${encodeURIComponent(ticker)}`);
        } catch (error) {
            alert("Failed to fetch data for the selected security. Please pick another security.");
        }
    };

    useEffect(() => {
    }, []);

    const handleSearchInput = (event) => {
        const value = event.target.value;
        setSearchText(value);
        updateSuggestions(value);
    };

    const handleKeyDown = (event) => {
        if (event.key === 'Enter') {
            event.preventDefault();
            if (suggestions.length > 0) {
                handleSuggestionClick(suggestions[0]);
            } else {
                setSearchText('');
                alert("Please type the security name or ticker symbol again.");
            }
        }
    };

    const getTextWidth = (text, font) => {
        const canvas = getTextWidth.canvas || (getTextWidth.canvas = document.createElement("canvas"));
        const context = canvas.getContext("2d");
        context.font = font;
        const metrics = context.measureText(text);
        return metrics.width;
    };

    const updateSuggestions = async (value) => {
        if (value.length > 1) {
            try {
                const response = await axios.get(`http://tradesim-api.adityakmehrotra.com/paper_trader/security/suggestion/${value}`);
                if (response.data && response.data.length) {
                    const inputWidth = searchInputRef.current ? searchInputRef.current.offsetWidth - 88 : 0;
                    setSuggestions(response.data.map(item => {
                        const fullName = `${item.name} (${item.code})`;
                        const font = "14px Arial";
                        const textWidth = getTextWidth(fullName, font);
                        if (textWidth > inputWidth) {
                            const adjustedName = `${item.name.substring(0, item.name.length - (textWidth - inputWidth) / 7)}...`;
                            return `${adjustedName} (${item.code})`;
                        }
                        return fullName;
                    }));
                } else {
                    setSuggestions([]);
                }
            } catch (error) {
                console.error('Failed to fetch suggestions:', error);
                setSuggestions([]);
            }
        } else {
            setSuggestions([]);
        }
    };

    const handleSuggestionClick = (suggestion) => {
        const suggestingTicker = suggestion.match(/\(([^)]+)\)/)[1];
        setSearchText('');
        handleSearch(suggestingTicker.toLowerCase());
        setSuggestions([]);
    };

    return (
        <div>
            <Navbar bg="dark" variant="dark" expand="lg" fixed="top">
                <Container fluid>
                    <Navbar.Brand as={Link} to="/">TradeSim</Navbar.Brand>
                    <Navbar.Toggle aria-controls="basic-navbar-nav" />
                    <Navbar.Collapse id="basic-navbar-nav">
                        <Nav className="me-auto" />
                        <Form className="d-flex mx-auto position-relative" style={{ width: '30%' }} onKeyDown={handleKeyDown}>
                            <FormControl
                                type="text"
                                placeholder="Search"
                                className="mr-2"
                                style={{ width: '80%' }}
                                ref={searchInputRef}
                                value={searchText}
                                onChange={handleSearchInput}
                            />
                            <Button variant="outline-info" style={{ width: '20%' }} onClick={() => {
                                if (suggestions.length > 0) {
                                    handleSuggestionClick(suggestions[0]);
                                } else {
                                    setSearchText('');
                                    alert("Please type the security name or ticker symbol again.");
                                }
                            }}>Search</Button>
                            {suggestions.length > 0 && (
                                <Dropdown.Menu show style={{ position: 'absolute', top: '38px', left: '0', width: '100%' }}>
                                    {suggestions.map((suggestion, index) => (
                                        <Dropdown.Item key={index} onClick={() => handleSuggestionClick(suggestion)}>
                                            {suggestion}
                                        </Dropdown.Item>
                                    ))}
                                </Dropdown.Menu>
                            )}
                        </Form>
                        <Nav className="ms-auto">
                            <Nav.Link as={NavLink} to="/portfolio" className={({ isActive }) => isActive ? "active" : ""}>Portfolio</Nav.Link>
                            <Nav.Link as={NavLink} to="/notifications" className={({ isActive }) => isActive ? "active" : ""}>Notifications</Nav.Link>
                            <Nav.Link as={NavLink} to="/account" className={({ isActive }) => isActive ? "active" : ""}>Account</Nav.Link>
                        </Nav>
                    </Navbar.Collapse>
                </Container>
            </Navbar>
            <Container style={{ paddingTop: '76px' }}>
                <Outlet />
            </Container>
        </div>
    );
}
