package com.adityamehrotra.tradesim.service;

import com.adityamehrotra.tradesim.model.Counter;
import com.adityamehrotra.tradesim.model.Portfolio;
import com.adityamehrotra.tradesim.model.Session;
import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

/**
 * Hands out the numeric ids. Reading the highest id and adding one lets two requests that overlap
 * pick the same number, so the number comes from a counter that Mongo increments atomically.
 */
@Service
public class SequenceService {
  public static final String ACCOUNT_ID = "accountID";
  public static final String PORTFOLIO_ID = "portfolioID";

  private final MongoTemplate mongoTemplate;

  public SequenceService(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  /**
   * Brings the counters up to whatever is already stored, so a database written by the old
   * behaviour keeps counting from where it left off. Runs while the bean is being created, which is
   * before the web server accepts anything, so no request can allocate against an unseeded counter.
   * The update only ever raises a counter, which makes running it again harmless.
   */
  @PostConstruct
  public void seedFromExistingData() {
    seed(ACCOUNT_ID, highest(Session.class, ACCOUNT_ID, session -> session.getAccountID()));
    seed(PORTFOLIO_ID, highest(Portfolio.class, PORTFOLIO_ID, Portfolio::getPortfolioID));
  }

  /** Returns the next id for the named sequence. */
  public int next(String name) {
    Counter counter =
        mongoTemplate.findAndModify(
            Query.query(Criteria.where("_id").is(name)),
            new Update().inc("seq", 1),
            FindAndModifyOptions.options().returnNew(true).upsert(true),
            Counter.class);
    return counter.getSeq();
  }

  private void seed(String name, int current) {
    mongoTemplate.upsert(
        Query.query(Criteria.where("_id").is(name)),
        new Update().max("seq", current),
        Counter.class);
  }

  private <T> int highest(Class<T> type, String field, java.util.function.ToIntFunction<T> id) {
    Query query = new Query().with(Sort.by(Sort.Direction.DESC, field)).limit(1);
    T last = mongoTemplate.findOne(query, type);
    return last == null ? 0 : id.applyAsInt(last);
  }
}
