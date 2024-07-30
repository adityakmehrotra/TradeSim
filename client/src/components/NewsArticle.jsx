import React, { useRef, useState, useEffect } from 'react';
import { Card, Col } from 'react-bootstrap';
import moment from 'moment';

function NewsArticle({ article }) {
  const imageRef = useRef(null);
  const [maxHeight, setMaxHeight] = useState('150px');
  const [hover, setHover] = useState(false);

  useEffect(() => {
    if (imageRef.current) {
      setMaxHeight(`${imageRef.current.offsetHeight}px`);
    }
  }, []);

  const cardStyle = {
    width: '100%',
    marginBottom: '20px',
    display: 'flex',
    flexDirection: 'row',
    maxHeight: maxHeight,
    overflow: 'hidden',
    border: 'none',
    backgroundColor: hover ? '#f8f9fa' : 'transparent',
    transition: 'background-color 0.3s'
  };

  return (
    <a href={article.article_url} target="_blank" rel="noopener noreferrer" style={{ textDecoration: 'none', color: 'inherit' }}>
      <Card 
        style={cardStyle}
        onMouseEnter={() => setHover(true)}
        onMouseLeave={() => setHover(false)}
      >
        <Col xs={8} style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
          <Card.Body>
            <Card.Title style={{ fontSize: '1rem' }}>
              {article.publisher.name} - {moment(article.published_utc).fromNow()}
            </Card.Title>
            <Card.Text style={{
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              display: '-webkit-box',
              WebkitLineClamp: 3,
              WebkitBoxOrient: 'vertical'
            }}>
              {article.description}
            </Card.Text>
          </Card.Body>
        </Col>
        <Col xs={4} style={{ padding: 0 }}>
          <Card.Img ref={imageRef} style={{ height: '150px', width: '100%', objectFit: 'cover' }} src={article.image_url} alt="Article" />
        </Col>
      </Card>
    </a>
  );
}

export default NewsArticle;