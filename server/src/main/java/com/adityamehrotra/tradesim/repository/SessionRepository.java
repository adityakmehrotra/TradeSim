package com.adityamehrotra.tradesim.repository;

import com.adityamehrotra.tradesim.model.Session;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends MongoRepository<Session, String> {}
