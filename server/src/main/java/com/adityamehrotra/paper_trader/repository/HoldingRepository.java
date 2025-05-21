package com.adityamehrotra.paper_trader.repository;

import com.adityamehrotra.paper_trader.model.Holding;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HoldingRepository extends MongoRepository<Holding, Integer> {
    boolean existsByHoldingID(@NotEmpty(message = "Holding ID cannot be empty") Integer holdingID);
    Holding findByHoldingID(@NotEmpty(message = "Holding ID cannot be empty") Integer holdingID);
}
