package com.adityamehrotra.tradesim.repository;

import com.adityamehrotra.tradesim.model.Position;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository extends MongoRepository<Position, String> {
  List<Position> findByPortfolioID(Integer portfolioID);

  Position findByPortfolioIDAndSymbol(Integer portfolioID, String symbol);
}
