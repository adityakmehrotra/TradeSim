package com.adityamehrotra.paper_trader.service;

import com.adityamehrotra.paper_trader.dto.HoldingRequest;
import com.adityamehrotra.paper_trader.dto.UpdateHoldingRequest;
import com.adityamehrotra.paper_trader.model.Holding;
import com.adityamehrotra.paper_trader.repository.HoldingRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class HoldingService {
    private final HoldingRepository holdingRepository;
    private final MongoTemplate mongoTemplate;

    public HoldingService(HoldingRepository holdingRepository, MongoTemplate mongoTemplate) {
        this.holdingRepository = holdingRepository;
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

        List<Integer> transactionList = new ArrayList<>();
        transactionList.add(holding.getTransactionID());

        Holding newHolding = new Holding(
                holding.getHoldingID(),
                holding.getPortfolioID(),
                transactionList,
                holding.getShares(),
                holding.getPrice(),
                true
        );

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

        List<Integer> transactionList = holding.getTransactionList();
        transactionList.add(updateHoldingRequest.getTransactionID());
        holding.setTransactionList(transactionList);

        if (updateHoldingRequest.getAction().equalsIgnoreCase("buy")) {
            holding.setShares(holding.getShares() + updateHoldingRequest.getShares());
            if (holding.getActive()) {
                holding.setAvgValue((holding.getAvgValue() * holding.getShares() + updateHoldingRequest.getPrice() * updateHoldingRequest.getShares()) / (holding.getShares() + updateHoldingRequest.getShares()));
            } else {
                holding.setAvgValue(updateHoldingRequest.getPrice());
                holding.setActive(true);
            }
        } else if (updateHoldingRequest.getAction().equalsIgnoreCase("sell")) {
            if (holding.getShares() < updateHoldingRequest.getShares()) {
                throw new IllegalArgumentException("Not enough shares to sell");
            } else if (holding.getShares() == updateHoldingRequest.getShares()) {
                holding.setActive(false);
            }
            holding.setShares(holding.getShares() - updateHoldingRequest.getShares());
        } else {
            throw new IllegalArgumentException("Invalid action");
        }
    }

    @Transactional
    public boolean existsByHoldingID(int holdingID) {
        return holdingRepository.existsByHoldingID(holdingID);
    }
}
