package com.adityamehrotra.paper_trader.repository;

import com.adityamehrotra.paper_trader.model.PaperTrader;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaperTraderRepository extends MongoRepository<PaperTrader, String> {
  @Query("SELECT MAX(p.id) FROM PaperTrader p")
  Integer findMaxId();
}
