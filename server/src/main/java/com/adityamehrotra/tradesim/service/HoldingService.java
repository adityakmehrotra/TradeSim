package com.adityamehrotra.tradesim.service;

import com.adityamehrotra.tradesim.dto.HoldingRequest;
import com.adityamehrotra.tradesim.dto.UpdateHoldingRequest;
import com.adityamehrotra.tradesim.model.Holding;
import com.adityamehrotra.tradesim.model.Portfolio;
import com.adityamehrotra.tradesim.repository.HoldingRepository;
import com.adityamehrotra.tradesim.repository.PortfolioRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HoldingService {
  private final HoldingRepository holdingRepository;
  private final PortfolioRepository portfolioRepository;
  private final MongoTemplate mongoTemplate;

  public HoldingService(
      HoldingRepository holdingRepository,
      PortfolioRepository portfolioRepository,
      MongoTemplate mongoTemplate) {
    this.holdingRepository = holdingRepository;
    this.portfolioRepository = portfolioRepository;
    this.mongoTemplate = mongoTemplate;
  }

  @Transactional
  public int getNextID() {
    Query query = new Query();
    query.with(Sort.by(Sort.Direction.DESC, "holdingID"));
    query.limit(1);

    Holding lastHolding = mongoTemplate.findOne(query, Holding.class);
    if (lastHolding == null) {
      return 1;
    } else {
      return lastHolding.getHoldingID() + 1;
    }
  }

  @Transactional
  public void createHolding(HoldingRequest holding) {
    if (holding == null) {
      throw new IllegalArgumentException("Holding cannot be null");
    }

    if (holding.getHoldingID() == null || holding.getHoldingID() <= 0) {
      throw new IllegalArgumentException("Asset ID must be greater than 0");
    }

    if (holding.getPortfolioID() == null || holding.getPortfolioID() <= 0) {
      throw new IllegalArgumentException("Portfolio ID must be greater than 0");
    }

    if (holding.getTransactionID() == null || holding.getTransactionID() <= 0) {
      throw new IllegalArgumentException("Transaction ID cannot be null or empty");
    }

    if (holding.getShares() == null || holding.getShares() <= 0) {
      throw new IllegalArgumentException("Shares must be greater than 0");
    }

    if (holding.getPrice() == null || holding.getPrice() <= 0) {
      throw new IllegalArgumentException("Price must be greater than 0");
    }

    Portfolio portfolio = portfolioRepository.findByPortfolioID(holding.getPortfolioID());
    if (portfolio == null) {
      throw new IllegalArgumentException("Portfolio not found");
    }

    double cost = holding.getShares() * holding.getPrice();
    if (portfolio.getCash() < cost) {
      throw new IllegalArgumentException("Insufficient cash for this purchase");
    }

    List<Integer> transactionList = new ArrayList<>();
    transactionList.add(holding.getTransactionID());

    Holding newHolding =
        new Holding(
            holding.getHoldingID(),
            holding.getPortfolioID(),
            transactionList,
            holding.getShares(),
            holding.getPrice(),
            true);

    portfolio.setCash(portfolio.getCash() - cost);
    portfolioRepository.save(portfolio);
    holdingRepository.save(newHolding);
  }

  @Transactional
  public Holding getHolding(Integer id) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("Invalid Holding ID");
    }

    Holding holding = holdingRepository.findByHoldingID(id);

    if (holding == null) {
      throw new IllegalArgumentException("Holding not found");
    }

    return holding;
  }

  @Transactional
  public void updateHolding(UpdateHoldingRequest updateHoldingRequest) {
    if (updateHoldingRequest.getHoldingID() <= 0) {
      throw new IllegalArgumentException("Invalid Holding ID");
    }

    if (updateHoldingRequest.getShares() <= 0) {
      throw new IllegalArgumentException("Shares must be greater than 0");
    }

    if (updateHoldingRequest.getPrice() <= 0) {
      throw new IllegalArgumentException("Price must be greater than 0");
    }

    Holding holding = holdingRepository.findByHoldingID(updateHoldingRequest.getHoldingID());

    if (holding == null) {
      throw new IllegalArgumentException("Holding not found");
    }

    Portfolio portfolio = portfolioRepository.findByPortfolioID(holding.getPortfolioID());
    if (portfolio == null) {
      throw new IllegalArgumentException("Portfolio not found");
    }

    double tradeValue = updateHoldingRequest.getShares() * updateHoldingRequest.getPrice();

    List<Integer> transactionList = holding.getTransactionList();
    transactionList.add(updateHoldingRequest.getTransactionID());
    holding.setTransactionList(transactionList);

    if (updateHoldingRequest.getAction().equalsIgnoreCase("buy")) {
      if (portfolio.getCash() < tradeValue) {
        throw new IllegalArgumentException("Insufficient cash for this purchase");
      }

      if (holding.getActive()) {
        double currentShares = holding.getShares();
        double addedShares = updateHoldingRequest.getShares();
        double newAvgValue =
            (holding.getAvgValue() * currentShares + updateHoldingRequest.getPrice() * addedShares)
                / (currentShares + addedShares);
        holding.setAvgValue(newAvgValue);
      } else {
        holding.setAvgValue(updateHoldingRequest.getPrice());
        holding.setActive(true);
      }

      holding.setShares(holding.getShares() + updateHoldingRequest.getShares());
      portfolio.setCash(portfolio.getCash() - tradeValue);
    } else if (updateHoldingRequest.getAction().equalsIgnoreCase("sell")) {
      if (holding.getShares() < updateHoldingRequest.getShares()) {
        throw new IllegalArgumentException("Not enough shares to sell");
      }

      holding.setShares(holding.getShares() - updateHoldingRequest.getShares());
      if (holding.getShares() <= 0) {
        holding.setShares(0.0);
        holding.setActive(false);
        holding.setAvgValue(0.0);
      }

      portfolio.setCash(portfolio.getCash() + tradeValue);
    } else {
      throw new IllegalArgumentException("Invalid action");
    }

    holdingRepository.save(holding);
    portfolioRepository.save(portfolio);
  }

  @Transactional
  public boolean existsByHoldingID(int holdingID) {
    return holdingRepository.existsByHoldingID(holdingID);
  }
}
