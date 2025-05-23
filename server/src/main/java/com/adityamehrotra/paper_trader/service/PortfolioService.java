package com.adityamehrotra.paper_trader.service;

import com.adityamehrotra.paper_trader.dto.PortfolioRequest;
import com.adityamehrotra.paper_trader.model.Portfolio;
import com.adityamehrotra.paper_trader.model.User;
import com.adityamehrotra.paper_trader.repository.PortfolioRepository;
import com.adityamehrotra.paper_trader.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    public PortfolioService(PortfolioRepository portfolioRepository, UserRepository userRepository, MongoTemplate mongoTemplate, MongoTemplate mongoTemplate1) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate1;
    }

    @Transactional
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

    @Transactional
    public int createPortfolio(PortfolioRequest portfolio) {
        if (portfolio.getAccountID() == null || portfolio.getAccountID() <= 0) {
            throw new IllegalArgumentException("Account ID cannot be empty or less than 1");
        }

        if (portfolio.getName() == null || portfolio.getName().isEmpty()) {
            throw new IllegalArgumentException("Portfolio name cannot be empty");
        }

        if (portfolio.getDescription() == null || portfolio.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Portfolio name cannot be empty");
        }

        if (portfolio.getCash() == null || portfolio.getCash() < 0) {
            throw new IllegalArgumentException("Cash amount cannot be empty or less than 0");
        }

        if (portfolio.getInitialBalance() == null || portfolio.getInitialBalance() <= 0) {
            throw new IllegalArgumentException("Initial balance cannot be empty or less than 1");
        }

        int portfolioID = getNextID();

        List<Integer> transactionList = new ArrayList<>();
        List<Integer> holdingsList = new ArrayList<>();

        Portfolio newPortfolio = new Portfolio(
                portfolioID,
                portfolio.getAccountID(),
                portfolio.getName(),
                portfolio.getDescription(),
                portfolio.getCash(),
                portfolio.getInitialBalance(),
                transactionList,
                holdingsList
        );

        portfolioRepository.save(newPortfolio);

        return portfolioID;
    }

    @Transactional
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

    @Transactional
    public void updatePortfolioName(Integer portfolioID, String name) {
        if (portfolioID == null || portfolioID <= 0) {
            throw new IllegalArgumentException("Invalid portfolio ID");
        }

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Portfolio name cannot be empty");
        }

        Portfolio portfolio = portfolioRepository.findByPortfolioID(portfolioID);

        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio not found");
        }

        portfolio.setName(name);
        portfolioRepository.save(portfolio);
    }

    @Transactional
    public void updatePortfolioDescription(Integer portfolioID, String description) {
        if (portfolioID == null || portfolioID <= 0) {
            throw new IllegalArgumentException("Invalid portfolio ID");
        }

        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("Portfolio description cannot be empty");
        }

        Portfolio portfolio = portfolioRepository.findByPortfolioID(portfolioID);

        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio not found");
        }

        portfolio.setDescription(description);
        portfolioRepository.save(portfolio);
    }

    @Transactional
    public void updatePortfolioCash(Integer portfolioID, Double cash) {
        if (portfolioID == null || portfolioID <= 0) {
            throw new IllegalArgumentException("Invalid portfolio ID");
        }

        if (cash == null || cash < 0) {
            throw new IllegalArgumentException("Cash amount cannot be empty or less than 0");
        }

        Portfolio portfolio = portfolioRepository.findByPortfolioID(portfolioID);

        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio not found");
        }

        portfolio.setCash(cash);
        portfolioRepository.save(portfolio);
    }

    @Transactional
    public void deletePortfolio(Integer portfolioID) {
        if (portfolioID == null || portfolioID <= 0) {
            throw new IllegalArgumentException("Invalid portfolio ID");
        }

        Portfolio portfolio = portfolioRepository.findByPortfolioID(portfolioID);

        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio not found");
        }

        portfolioRepository.delete(portfolio);
    }

    @Transactional
    public void addTransaction(Integer portfolioID, Integer transactionID) {
        if (portfolioID == null || portfolioID <= 0) {
            throw new IllegalArgumentException("Invalid portfolio ID");
        }

        if (transactionID == null || transactionID <= 0) {
            throw new IllegalArgumentException("Invalid transaction ID");
        }

        Portfolio portfolio = portfolioRepository.findByPortfolioID(portfolioID);

        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio not found");
        }

        List<Integer> transactionList = portfolio.getTransactionList();
        if (!transactionList.contains(transactionID)) {
            transactionList.add(transactionID);
            portfolio.setTransactionList(transactionList);
            portfolioRepository.save(portfolio);
        } else {
            throw new IllegalArgumentException("Transaction already exists in the portfolio");
        }
    }

    @Transactional
    public void removeTransaction(Integer portfolioID, Integer transactionID) {
        if (portfolioID == null || portfolioID <= 0) {
            throw new IllegalArgumentException("Invalid portfolio ID");
        }

        if (transactionID == null || transactionID <= 0) {
            throw new IllegalArgumentException("Invalid transaction ID");
        }

        Portfolio portfolio = portfolioRepository.findByPortfolioID(portfolioID);

        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio not found");
        }

        List<Integer> transactionList = portfolio.getTransactionList();
        if (transactionList.contains(transactionID)) {
            transactionList.remove(transactionID);
            portfolio.setTransactionList(transactionList);
            portfolioRepository.save(portfolio);
        } else {
            throw new IllegalArgumentException("Transaction does not exist in the portfolio");
        }
    }

    @Transactional
    public List<Integer> getTransactionList(Integer portfolioID) {
        if (portfolioID == null || portfolioID <= 0) {
            throw new IllegalArgumentException("Invalid portfolio ID");
        }

        Portfolio portfolio = portfolioRepository.findByPortfolioID(portfolioID);

        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio not found");
        }

        return portfolio.getTransactionList();
    }

    @Transactional
    public void clearTransactionList(Integer portfolioID) {
        if (portfolioID == null || portfolioID <= 0) {
            throw new IllegalArgumentException("Invalid portfolio ID");
        }

        Portfolio portfolio = portfolioRepository.findByPortfolioID(portfolioID);

        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio not found");
        }

        portfolio.setTransactionList(new ArrayList<>());
        portfolioRepository.save(portfolio);
    }

    @Transactional
    public void addHolding(Integer portfolioID, Integer holdingID) {
        if (portfolioID == null || portfolioID <= 0) {
            throw new IllegalArgumentException("Invalid portfolio ID");
        }

        if (holdingID == null || holdingID <= 0) {
            throw new IllegalArgumentException("Invalid holding ID");
        }

        Portfolio portfolio = portfolioRepository.findByPortfolioID(portfolioID);

        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio not found");
        }

        List<Integer> holdingsList = portfolio.getHoldingsList();
        if (!holdingsList.contains(holdingID)) {
            holdingsList.add(holdingID);
            portfolio.setHoldingsList(holdingsList);
            portfolioRepository.save(portfolio);
        } else {
            throw new IllegalArgumentException("Holding already exists in the portfolio");
        }
    }

    @Transactional
    public void removeHolding(Integer portfolioID, Integer holdingID) {
        if (portfolioID == null || portfolioID <= 0) {
            throw new IllegalArgumentException("Invalid portfolio ID");
        }

        if (holdingID == null || holdingID <= 0) {
            throw new IllegalArgumentException("Invalid holding ID");
        }

        Portfolio portfolio = portfolioRepository.findByPortfolioID(portfolioID);

        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio not found");
        }

        List<Integer> holdingsList = portfolio.getHoldingsList();
        if (holdingsList.contains(holdingID)) {
            holdingsList.remove(holdingID);
            portfolio.setHoldingsList(holdingsList);
            portfolioRepository.save(portfolio);
        } else {
            throw new IllegalArgumentException("Holding does not exist in the portfolio");
        }
    }

    @Transactional
    public List<Integer> getHoldingList(Integer portfolioID) {
        if (portfolioID == null || portfolioID <= 0) {
            throw new IllegalArgumentException("Invalid portfolio ID");
        }

        Portfolio portfolio = portfolioRepository.findByPortfolioID(portfolioID);

        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio not found");
        }

        return portfolio.getHoldingsList();
    }

    @Transactional
    public void clearHoldingList(Integer portfolioID) {
        if (portfolioID == null || portfolioID <= 0) {
            throw new IllegalArgumentException("Invalid portfolio ID");
        }

        Portfolio portfolio = portfolioRepository.findByPortfolioID(portfolioID);

        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio not found");
        }

        portfolio.setHoldingsList(new ArrayList<>());
        portfolioRepository.save(portfolio);
    }
}
