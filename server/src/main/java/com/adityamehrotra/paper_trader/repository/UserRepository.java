package com.adityamehrotra.paper_trader.repository;

import com.adityamehrotra.paper_trader.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, Integer> {}
