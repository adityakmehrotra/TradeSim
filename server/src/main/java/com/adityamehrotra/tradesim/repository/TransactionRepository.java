package com.adityamehrotra.tradesim.repository;

import com.adityamehrotra.tradesim.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, Integer> {}
