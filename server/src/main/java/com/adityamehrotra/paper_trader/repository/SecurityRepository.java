package com.adityamehrotra.paper_trader.repository;

import com.adityamehrotra.paper_trader.model.SecurityModelLegacy;
import java.util.Set;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface SecurityRepository extends MongoRepository<SecurityModelLegacy, String> {
  @Query(
      "{'$or': [{'_id': {'$regex': '^?0', '$options': 'i'}}, {'name': {'$regex': '^?0', '$options': 'i'}}]}")
  Set<SecurityModelLegacy> findSecuritiesStartingWith(String userInput);
}
