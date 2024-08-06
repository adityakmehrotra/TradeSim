package com.adityamehrotra.paper_trader.repository;

import com.adityamehrotra.paper_trader.model.PaperTraderID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDRepository extends MongoRepository<PaperTraderID, String> {}
