package com.adityamehrotra.paper_trader.repository;

import com.adityamehrotra.paper_trader.model.Portfolio;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioRepository extends MongoRepository<Portfolio, Integer> {}
