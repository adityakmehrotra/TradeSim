import React, { useEffect, useState, useContext } from 'react';
import { Container, Row, Col, Button } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import NewsArticle from './NewsArticle';
import KeyStatistics from './KeyStatistics';
import AboutCompany from './AboutCompany';
import TradingPage from './TradingPage';
import StockChart from './StockChart';
import { UserContext } from '../UserContext';

export default function SecurityPage({ setActiveTab }) {
    const { user, id } = useContext(UserContext);
    const [visibleCount, setVisibleCount] = useState(3);
    const [newsArticles, setNewsArticles] = useState([]);
    const [keyStats, setKeyStats] = useState(null);
    const [companyData, setCompanyData] = useState(null);
    const [currentPrice, setCurrentPrice] = useState(null);
    const [startingPrice, setStartingPrice] = useState(null);
    const [selectedRange, setSelectedRange] = useState('1D');
    const { searchQuery } = useParams();
    const ticker = searchQuery.toUpperCase();
    const navigate = useNavigate();

    const backendURL = process.env.REACT_APP_BACKEND_URL;
    const apiKey = process.env.REACT_APP_API_KEY;

    useEffect(() => {
        getCompanyData();
        getQuoteEndpoint();
        getKeyStatisticsEndpoint();
        fetchCurrentPrice();
        fetchStartingPrice();
    }, [searchQuery, selectedRange]);    

    useEffect(() => {
        if (currentPrice !== null) {
            setActiveTab(`${ticker} - $${currentPrice.toFixed(2)} | TradeSim`);
        }
    }, [currentPrice, ticker, setActiveTab]);

    const getCompanyData = () => {
        fetch(`https://api.polygon.io/v3/reference/tickers/${ticker}?apiKey=${apiKey}`)
        .then(response => response.json())
        .then(data => {
            if (data.status === "OK") {
                setCompanyData(data.results);
            }
        })
        .catch(error => console.error('Error fetching company data:', error));
    };

    const getKeyStatisticsEndpoint = () => {
        fetch(`https://api.polygon.io/v2/aggs/ticker/${ticker}/prev?adjusted=true&apiKey=${apiKey}`, {
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
        fetch(`https://api.polygon.io/v2/reference/news?ticker=${ticker}&order=desc&limit=50&apiKey=${apiKey}`, {
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
        fetch(`https://api.polygon.io/v2/snapshot/locale/us/markets/stocks/tickers/${ticker}?apiKey=${apiKey}`)
        .then(response => response.json())
        .then(data => {
            if (data && data.ticker && data.ticker.day && data.ticker.day.c) {
                setCurrentPrice(data.ticker.day.c);
                console.log('Current Price:', data.ticker.day.c);
            }

        })
        .catch(error => console.error('Error fetching current price:', error));
    };

    const fetchStartingPrice = () => {
        const [multiplier, timespan, from, to] = getIntervalParams(selectedRange);
        fetch(`https://api.polygon.io/v2/aggs/ticker/${ticker}/range/${multiplier}/${timespan}/${from}/${to}?adjusted=true&sort=asc&limit=50000&apiKey=${apiKey}`)
            .then(response => response.json())
            .then(data => {
                if (data.results && data.results.length > 0) {
                    setStartingPrice(data.results[0].c);
                }
            })
            .catch(error => console.error("Error fetching starting price:", error));
    };

    const getIntervalParams = (range) => {
        let multiplier, timespan, fromDate, toDate;
        toDate = new Date();
        fromDate = new Date();

        switch (range) {
            case '1D':
                fromDate.setDate(toDate.getDate() - 1);
                timespan = 'minute';
                multiplier = 1;
                break;
            case '1W':
                fromDate.setDate(toDate.getDate() - 7);
                timespan = 'hour';
                multiplier = 1;
                break;
            case '1M':
                fromDate.setMonth(toDate.getMonth() - 1);
                timespan = 'hour';
                multiplier = 1;
                break;
            case '3M':
                fromDate.setMonth(toDate.getMonth() - 3);
                timespan = 'day';
                multiplier = 1;
                break;
            case 'YTD':
                fromDate = new Date(new Date().getFullYear(), 0, 1);
                timespan = 'day';
                multiplier = 1;
                break;
            case '1Y':
                fromDate.setFullYear(toDate.getFullYear() - 1);
                timespan = 'day';
                multiplier = 1;
                break;
            case '5Y':
                fromDate.setFullYear(toDate.getFullYear() - 5);
                timespan = 'week';
                multiplier = 1;
                break;
            default:
                fromDate.setDate(toDate.getDate() - 1);
                timespan = 'minute';
                multiplier = 1;
                break;
        }

        const from = fromDate.toISOString().split('T')[0];
        const to = toDate.toISOString().split('T')[0];

        return [multiplier, timespan, from, to];
    };

    const calculatePriceChange = () => {
        if (currentPrice !== null && startingPrice !== null) {
            return Math.abs((currentPrice - startingPrice)).toFixed(2);
        }
        return null;
    };

    const calculatePercentChange = () => {
        if (currentPrice !== null && startingPrice !== null) {
            return Math.abs((currentPrice - startingPrice) / startingPrice * 100).toFixed(2);
        }
        return null;
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
                    {currentPrice !== null && startingPrice !== null && (
                      <h3 style={{ color: currentPrice - startingPrice >= 0 ? "green" : "red" }}>
                        {currentPrice - startingPrice >= 0 ? "+ $" : "- $"}{calculatePriceChange()}{currentPrice - startingPrice >= 0 ? " (+" : " (-"}{calculatePercentChange() + "%)"}
                      </h3>
                    )}
                  </Col>
                </Row>
              )}
              <StockChart ticker={ticker} selectedRange={selectedRange} setSelectedRange={setSelectedRange} />
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
            <Col md={3} style={{ maxWidth: '300px', width: '100%' }}>
              <TradingPage ticker={ticker} currentPrice={currentPrice} style={{ maxWidth: '100%' }} />
            </Col>
          </Row>
        </Container>
      );
      
}