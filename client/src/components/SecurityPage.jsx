import React, { useEffect, useState, useContext } from 'react';
import { Container, Row, Col, Button } from 'react-bootstrap';
import { useParams } from 'react-router-dom';
import NewsArticle from './NewsArticle';
import KeyStatistics from './KeyStatistics';
import AboutCompany from './AboutCompany';
import TradingPage from './TradingPage';
import StockChart from './StockChart';
import { UserContext } from '../UserContext';

export default function SecurityPage(props) {
    const { user } = useContext(UserContext);
    const [visibleCount, setVisibleCount] = useState(3);
    const [newsArticles, setNewsArticles] = useState([]);
    const [keyStats, setKeyStats] = useState(null);
    const [companyData, setCompanyData] = useState(null);
    const [currentPrice, setCurrentPrice] = useState(null);
    const { searchQuery } = useParams();
    const ticker = searchQuery.toUpperCase();

    useEffect(() => {
        getCompanyData();
        getQuoteEndpoint();
        getKeyStatisticsEndpoint();
        fetchCurrentPrice();
    }, [searchQuery]);    

    const getCompanyData = () => {
        fetch(`http://localhost:8000/paper_trader/polygon/companyData?ticker=${ticker}`)
        .then(response => response.json())
        .then(data => {
            if (data.status === "OK") {
                setCompanyData(data.results);
            }
        })
        .catch(error => console.error('Error fetching company data:', error));
    };

    const getKeyStatisticsEndpoint = () => {
        fetch(`http://localhost:8000/paper_trader/polygon/keyStatistics?ticker=${ticker}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            },
        })
        .then(response => response.json())
        .then(data => {
            if (data.results && data.results.length > 0) {
                setKeyStats(data.results[0]);
            }
        })
        .catch(error => {
            console.error('Error fetching key statistics:', error);
        });
    };

    const getQuoteEndpoint = () => {
        fetch(`http://localhost:8000/paper_trader/polygon/news?ticker=${ticker}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            },
        })
        .then(response => response.json())
        .then(data => {
            if (data && data.results && Array.isArray(data.results)) {
                setNewsArticles(data.results);
            } else {
                setNewsArticles([]);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            setNewsArticles([]);
        });
    };

    const fetchCurrentPrice = () => {
        console.log("Called");
        fetch(`http://localhost:8000/paper_trader/polygon/price?ticker=${ticker}`)
        .then(response => response.json())
        .then(data => {
            if (data && data.ticker && data.ticker.day && data.ticker.day.c) {
                setCurrentPrice(data.ticker.day.c);
                console.log('Current Price:', data.ticker.day.c);
            }
        })
        .catch(error => console.error('Error fetching current price:', error));
    };

    const showMoreArticles = () => {
        setVisibleCount(prevCount => prevCount + 5);
    };

    return (
        <Container fluid style={{ paddingLeft: '5%', paddingRight: '15%', paddingTop: '20px', paddingBottom: '20px' }}>
            <Row>
                <Col md={9} style={{ paddingRight: '1%' }}>
                    {companyData && (
                        <Row className="align-items-center mb-4">
                            <Col>
                                <h1>{companyData.name} ({companyData.ticker})</h1>
                                {currentPrice !== null && <h2>${currentPrice.toFixed(2)}</h2>}
                                {user && <h3>Logged in as: {user.username}</h3>}
                            </Col>
                        </Row>
                    )}
                    <StockChart ticker={ticker} />
                    <AboutCompany company={companyData} />
                    <Row className="mb-4">
                        <Col><h2 style={{ borderBottom: '2px solid #000' }}>Key Statistics</h2></Col>
                    </Row>
                    <Row className="mb-4">
                        <KeyStatistics stats={keyStats} ticker={ticker} />
                    </Row>
                    <Row className="mb-4">
                        <Col><h2 style={{ borderBottom: '2px solid #000' }}>News</h2></Col>
                    </Row>
                    {newsArticles && newsArticles.length > 0 && newsArticles.slice(0, visibleCount).map(article => (
                        <NewsArticle key={article.id} article={article} />
                    ))}
                    {visibleCount < newsArticles.length && (
                        <Button onClick={showMoreArticles} style={{ marginTop: '20px', display: 'block', marginLeft: 'auto', marginRight: 'auto' }}>Show More</Button>
                    )}
                </Col>
                <TradingPage ticker={ticker} currentPrice={currentPrice} />
            </Row>
        </Container>
    );
}
