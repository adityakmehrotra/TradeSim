package com.adityamehrotra.paper_trader.repository;

import com.adityamehrotra.paper_trader.model.SecurityModel;
import java.util.Set;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface SecurityRepository extends MongoRepository<SecurityModel, String> {
  @Query(
      "{'$or': [{'_id': {'$regex': '^?0', '$options': 'i'}}, {'name': {'$regex': '^?0', '$options': 'i'}}]}")
  Set<SecurityModel> findSecuritiesStartingWith(String userInput);
}
