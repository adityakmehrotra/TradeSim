package com.adityamehrotra.tradesim.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.adityamehrotra.tradesim.model.Portfolio;
import com.adityamehrotra.tradesim.model.Position;
import com.adityamehrotra.tradesim.service.CentsMigration;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

/** Documents written before money became integral have to come across exactly, and only once. */
class CentsMigrationIntegrationTest extends IntegrationTestBase {
  private static final String PORTFOLIOS = "TradeSim-Portfolio";
  private static final String POSITIONS = "TradeSim-Position";

  @Autowired private CentsMigration migration;
  @Autowired private MongoTemplate mongoTemplate;

  /** Writes the shape the previous version stored: dollars, and no cents fields at all. */
  private void insertLegacyPortfolio(int id, double cash, double initialBalance, double reserved) {
    mongoTemplate
        .getDb()
        .getCollection(PORTFOLIOS)
        .insertOne(
            new Document("_id", id)
                .append("accountID", id)
                .append("name", "Legacy")
                .append("description", "written before cents")
                .append("cash", cash)
                .append("initialBalance", initialBalance)
                .append("reservedCash", reserved));
  }

  @Test
  void bringsLegacyDollarsAcrossToExactCents() {
    insertLegacyPortfolio(1, 1234.56, 10000.0, 78.90);

    migration.migrate();

    Portfolio migrated = mongoTemplate.findById(1, Portfolio.class);
    assertEquals(123456, migrated.getCashCents());
    assertEquals(1000000, migrated.getInitialBalanceCents());
    assertEquals(7890, migrated.getReservedCashCents());
  }

  @Test
  void leavesTheDollarFieldsInStepSoARollbackStillReads() {
    insertLegacyPortfolio(2, 500.25, 1000.0, 0.0);

    migration.migrate();

    Document stored =
        mongoTemplate.getDb().getCollection(PORTFOLIOS).find(new Document("_id", 2)).first();
    assertEquals(50025L, stored.getLong("cashCents"));
    assertEquals(500.25, stored.getDouble("cash"));
  }

  @Test
  void runningItAgainChangesNothing() {
    insertLegacyPortfolio(3, 99.99, 5000.0, 1.01);

    migration.migrate();
    Document once =
        mongoTemplate.getDb().getCollection(PORTFOLIOS).find(new Document("_id", 3)).first();
    migration.migrate();
    migration.migrate();
    Document thrice =
        mongoTemplate.getDb().getCollection(PORTFOLIOS).find(new Document("_id", 3)).first();

    assertEquals(once, thrice);
  }

  /**
   * A rollback to the previous version writes fresh documents with no cents on them. The repair has
   * to notice those, which it would not if it trusted a stored version number.
   */
  @Test
  void repairsLegacyDocumentsWrittenAfterTheMigrationAlreadyRan() {
    insertLegacyPortfolio(4, 10.0, 1000.0, 0.0);
    migration.migrate();

    insertLegacyPortfolio(5, 42.42, 2000.0, 0.0);
    migration.migrate();

    Portfolio late = mongoTemplate.findById(5, Portfolio.class);
    assertEquals(4242, late.getCashCents());
  }

  @Test
  void bringsLegacyRealizedProfitAcross() {
    mongoTemplate
        .getDb()
        .getCollection(POSITIONS)
        .insertOne(
            new Document("_id", "9:AAPL")
                .append("portfolioID", 9)
                .append("symbol", "AAPL")
                .append("quantity", 5L)
                .append("reservedQuantity", 0L)
                .append("realizedPnl", -7.25));

    migration.migrate();

    Position migrated = mongoTemplate.findById("9:AAPL", Position.class);
    assertEquals(-725, migrated.getRealizedPnlCents());
  }

  @Test
  void recordsThatItRan() {
    insertLegacyPortfolio(6, 1.0, 1000.0, 0.0);

    migration.migrate();

    Document record =
        mongoTemplate
            .getDb()
            .getCollection("TradeSim-Migration")
            .find(new Document("_id", CentsMigration.VERSION))
            .first();
    assertNull(record.get("missing"));
    assertEquals(CentsMigration.VERSION, record.get("_id"));
  }
}
