package com.adityamehrotra.paper_trader.repository;

import com.adityamehrotra.paper_trader.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, Integer> {}
