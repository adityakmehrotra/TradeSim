package com.adityamehrotra.paper_trader.repository;

import com.adityamehrotra.paper_trader.model.Portfolio;
import com.adityamehrotra.paper_trader.model.User;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioRepository extends MongoRepository<Portfolio, Integer> {
    Portfolio findByPortfolioID(@NotEmpty(message = "Portfolio ID cannot be empty") Integer portfolioID);
}
