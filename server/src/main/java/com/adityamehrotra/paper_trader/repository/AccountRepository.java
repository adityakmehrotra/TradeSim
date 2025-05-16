package com.adityamehrotra.paper_trader.repository;

import com.adityamehrotra.paper_trader.model.AccountLegacy;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends MongoRepository<AccountLegacy, Integer> {}
