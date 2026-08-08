package com.adityamehrotra.tradesim.configs;

import com.adityamehrotra.tradesim.model.Session;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

/**
 * Index creation is not automatic in this version of Spring Data, so the ones that matter are
 * declared here. The unique account id is a backstop under the id counter: if a change ever handed
 * the same account id to two sessions, this refuses the write rather than letting them share an
 * account and, with it, each other's portfolios.
 */
@Component
public class MongoIndexes {
  private static final Logger log = LoggerFactory.getLogger(MongoIndexes.class);

  private final MongoTemplate mongoTemplate;

  public MongoIndexes(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @PostConstruct
  void createIndexes() {
    try {
      mongoTemplate
          .indexOps(Session.class)
          .ensureIndex(new Index().on("accountID", Sort.Direction.ASC).unique());
    } catch (DataAccessException e) {
      // Existing duplicates are the only realistic cause. Say so loudly and carry on, because the
      // counter is what prevents new ones and the application is still usable without the index.
      log.error("Could not create the unique index on session account ids: {}", e.getMessage());
    }
  }
}
