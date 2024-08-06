package com.adityamehrotra.paper_trader.repository;

import com.adityamehrotra.paper_trader.model.SpecAccount;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecAccountRepository extends MongoRepository<SpecAccount, String> {}
