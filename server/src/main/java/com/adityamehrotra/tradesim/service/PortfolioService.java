package com.adityamehrotra.tradesim.service;

import com.adityamehrotra.tradesim.dto.PortfolioRequest;
import com.adityamehrotra.tradesim.exception.PortfolioNotFoundException;
import com.adityamehrotra.tradesim.market.MarketService;
import com.adityamehrotra.tradesim.model.Portfolio;
import com.adityamehrotra.tradesim.repository.PortfolioRepository;
import com.adityamehrotra.tradesim.repository.PositionRepository;
import org.springframework.stereotype.Service;

@Service
public class PortfolioService {
  static final int MAX_NAME_LENGTH = 60;
  static final int MAX_DESCRIPTION_LENGTH = 200;
  static final int MAX_PORTFOLIOS_PER_ACCOUNT = 10;
  static final double MIN_INITIAL_BALANCE = 1000.0;
  static final double MAX_INITIAL_BALANCE = 1000000.0;

  private final PortfolioRepository portfolioRepository;
  private final PositionRepository positionRepository;
  private final MarketService marketService;
  private final SequenceService sequenceService;

  public PortfolioService(
      PortfolioRepository portfolioRepository,
      PositionRepository positionRepository,
      MarketService marketService,
      SequenceService sequenceService) {
    this.portfolioRepository = portfolioRepository;
    this.positionRepository = positionRepository;
    this.marketService = marketService;
    this.sequenceService = sequenceService;
  }

  public int getNextID() {
    return sequenceService.next(SequenceService.PORTFOLIO_ID);
  }

  public int createPortfolio(PortfolioRequest portfolio) {
    if (portfolio.getAccountID() == null || portfolio.getAccountID() <= 0) {
      throw new IllegalArgumentException("Account ID cannot be empty or less than 1");
    }

    String name = requireName(portfolio.getName());
    String description = requireDescription(portfolio.getDescription());

    if (portfolio.getInitialBalance() == null
        || portfolio.getInitialBalance() < MIN_INITIAL_BALANCE
        || portfolio.getInitialBalance() > MAX_INITIAL_BALANCE) {
      throw new IllegalArgumentException(
          "Initial balance must be between "
              + (long) MIN_INITIAL_BALANCE
              + " and "
              + (long) MAX_INITIAL_BALANCE);
    }

    if (portfolio.getCash() == null || portfolio.getCash() < 0) {
      throw new IllegalArgumentException("Cash amount cannot be empty or less than 0");
    }

    if (portfolioRepository.countByAccountID(portfolio.getAccountID())
        >= MAX_PORTFOLIOS_PER_ACCOUNT) {
      throw new IllegalArgumentException(
          "An account can hold at most " + MAX_PORTFOLIOS_PER_ACCOUNT + " portfolios");
    }

    int portfolioID = getNextID();

    Portfolio newPortfolio =
        new Portfolio(
            portfolioID,
            portfolio.getAccountID(),
            name,
            description,
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

  /**
   * Returns the portfolio only when it belongs to the given account. A portfolio owned by someone
   * else is reported exactly like one that does not exist, so ids cannot be enumerated.
   */
  public Portfolio requireOwned(Integer portfolioID, int accountID) {
    if (portfolioID == null || portfolioID <= 0) {
      throw new PortfolioNotFoundException();
    }

    Portfolio portfolio = portfolioRepository.findByPortfolioIDAndAccountID(portfolioID, accountID);

    if (portfolio == null) {
      throw new PortfolioNotFoundException();
    }

    return portfolio;
  }

  public void updatePortfolioName(Integer portfolioID, int accountID, String name) {
    Portfolio portfolio = requireOwned(portfolioID, accountID);
    portfolio.setName(requireName(name));
    portfolioRepository.save(portfolio);
  }

  public void updatePortfolioDescription(Integer portfolioID, int accountID, String description) {
    Portfolio portfolio = requireOwned(portfolioID, accountID);
    portfolio.setDescription(requireDescription(description));
    portfolioRepository.save(portfolio);
  }

  public void deletePortfolio(Integer portfolioID, int accountID) {
    deleteOwned(requireOwned(portfolioID, accountID));
  }

  /**
   * Clears everything that hangs off a portfolio before removing it. Resting orders go first so
   * their reserved cash and shares are released while the portfolio still exists, and the portfolio
   * document is deleted last, so a failure part way through leaves a portfolio to retry against
   * rather than orphaned positions and orders pointing at nothing.
   */
  public void deleteOwned(Portfolio portfolio) {
    int portfolioID = portfolio.getPortfolioID();
    marketService.cancelAllForPortfolio(portfolioID);
    positionRepository.deleteAll(positionRepository.findByPortfolioID(portfolioID));
    portfolioRepository.delete(portfolio);
  }

  private static String requireName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Portfolio name cannot be empty");
    }
    if (name.length() > MAX_NAME_LENGTH) {
      throw new IllegalArgumentException(
          "Portfolio name cannot be longer than " + MAX_NAME_LENGTH + " characters");
    }
    return name;
  }

  private static String requireDescription(String description) {
    if (description == null || description.isBlank()) {
      throw new IllegalArgumentException("Portfolio description cannot be empty");
    }
    if (description.length() > MAX_DESCRIPTION_LENGTH) {
      throw new IllegalArgumentException(
          "Portfolio description cannot be longer than " + MAX_DESCRIPTION_LENGTH + " characters");
    }
    return description;
  }
}
