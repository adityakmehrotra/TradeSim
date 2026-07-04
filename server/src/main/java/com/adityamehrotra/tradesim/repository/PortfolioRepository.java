package com.adityamehrotra.tradesim.repository;

import com.adityamehrotra.tradesim.model.Portfolio;
import com.adityamehrotra.tradesim.model.User;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioRepository extends MongoRepository<Portfolio, Integer> {
    Portfolio findByPortfolioID(@NotEmpty(message = "Portfolio ID cannot be empty") Integer portfolioID);
}
