package com.adityamehrotra.tradesim.service;

import com.adityamehrotra.tradesim.dto.PortfolioRequest;
import com.adityamehrotra.tradesim.model.Portfolio;
import com.adityamehrotra.tradesim.repository.PortfolioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class PortfolioService {

  private final PortfolioRepository portfolioRepository;
  private final MongoTemplate mongoTemplate;

  public PortfolioService(PortfolioRepository portfolioRepository, MongoTemplate mongoTemplate) {
    this.portfolioRepository = portfolioRepository;
    this.mongoTemplate = mongoTemplate;
  }

  public int getNextID() {
    Query query = new Query();
    query.with(Sort.by(Sort.Direction.DESC, "portfolioID"));
    query.limit(1);

    Portfolio lastPortfolio = mongoTemplate.findOne(query, Portfolio.class);
    if (lastPortfolio == null) {
      return 1;
    } else {
      return lastPortfolio.getPortfolioID() + 1;
    }
  }

  public int createPortfolio(PortfolioRequest portfolio) {
    if (portfolio.getAccountID() == null || portfolio.getAccountID() <= 0) {
      throw new IllegalArgumentException("Account ID cannot be empty or less than 1");
    }

    if (portfolio.getName() == null || portfolio.getName().isEmpty()) {
      throw new IllegalArgumentException("Portfolio name cannot be empty");
    }

    if (portfolio.getDescription() == null || portfolio.getDescription().isEmpty()) {
      throw new IllegalArgumentException("Portfolio description cannot be empty");
    }

    if (portfolio.getCash() == null || portfolio.getCash() < 0) {
      throw new IllegalArgumentException("Cash amount cannot be empty or less than 0");
    }

    if (portfolio.getInitialBalance() == null || portfolio.getInitialBalance() <= 0) {
      throw new IllegalArgumentException("Initial balance cannot be empty or less than 1");
    }

    int portfolioID = getNextID();

    Portfolio newPortfolio =
        new Portfolio(
            portfolioID,
            portfolio.getAccountID(),
            portfolio.getName(),
            portfolio.getDescription(),
            portfolio.getCash(),
            portfolio.getInitialBalance(),
            0.0);

    portfolioRepository.save(newPortfolio);

    return portfolioID;
  }

  public Portfolio getPortfolio(Integer id) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("Invalid portfolio ID");
    }

    Portfolio portfolio = portfolioRepository.findByPortfolioID(id);

    if (portfolio == null) {
      throw new IllegalArgumentException("Portfolio not found");
    }

    return portfolio;
  }

  public void updatePortfolioName(Integer portfolioID, String name) {
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("Portfolio name cannot be empty");
    }

    Portfolio portfolio = getPortfolio(portfolioID);
    portfolio.setName(name);
    portfolioRepository.save(portfolio);
  }

  public void updatePortfolioDescription(Integer portfolioID, String description) {
    if (description == null || description.isEmpty()) {
      throw new IllegalArgumentException("Portfolio description cannot be empty");
    }

    Portfolio portfolio = getPortfolio(portfolioID);
    portfolio.setDescription(description);
    portfolioRepository.save(portfolio);
  }

  public void deletePortfolio(Integer portfolioID) {
    portfolioRepository.delete(getPortfolio(portfolioID));
  }
}
