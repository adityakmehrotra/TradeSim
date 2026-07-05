package com.adityamehrotra.tradesim.repository;

import com.adityamehrotra.tradesim.model.Holding;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HoldingRepository extends MongoRepository<Holding, Integer> {
  boolean existsByHoldingID(@NotEmpty(message = "Holding ID cannot be empty") Integer holdingID);

  Holding findByHoldingID(@NotEmpty(message = "Holding ID cannot be empty") Integer holdingID);

  List<Holding> findByPortfolioID(Integer portfolioID);
}
