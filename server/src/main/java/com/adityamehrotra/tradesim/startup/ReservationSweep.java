package com.adityamehrotra.tradesim.startup;

import com.adityamehrotra.tradesim.model.Portfolio;
import com.adityamehrotra.tradesim.model.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

/**
 * Clears the cash and share reservations left behind by the previous run. Order books live in
 * memory and come back empty, so a reservation that survived a restart has no resting order behind
 * it and would hold buying power out of reach for good.
 *
 * <p>Two writes rather than a pass over every document: this has to finish before the first order
 * is accepted, and a round trip per portfolio turns a large database into a slow start. Setting a
 * field that already reads zero writes nothing, so a sweep that died halfway is fixed by running it
 * again.
 */
@Component
public class ReservationSweep {
  private static final Logger log = LoggerFactory.getLogger(ReservationSweep.class);

  private final MongoTemplate mongoTemplate;

  public ReservationSweep(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public void run() {
    long portfolios =
        mongoTemplate
            .updateMulti(
                Query.query(Criteria.where("reservedCashCents").ne(0L)),
                // The dollar field is still written for the rollback window, so it has to go to
                // zero with the cents or a rolled back build reads a reservation nothing holds.
                new Update().set("reservedCashCents", 0L).set("reservedCash", 0.0),
                Portfolio.class)
            .getModifiedCount();

    long positions =
        mongoTemplate
            .updateMulti(
                Query.query(Criteria.where("reservedQuantity").ne(0L)),
                new Update().set("reservedQuantity", 0L),
                Position.class)
            .getModifiedCount();

    if (portfolios > 0 || positions > 0) {
      log.info(
          "Released stale reservations on {} portfolios and {} positions", portfolios, positions);
    }
  }
}
