package com.adityamehrotra.tradesim.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.adityamehrotra.tradesim.model.Portfolio;
import com.adityamehrotra.tradesim.model.Position;
import com.adityamehrotra.tradesim.startup.ReservationSweep;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

/** Reservations that outlived their order book, cleared against a real database. */
class ReservationSweepIntegrationTest extends IntegrationTestBase {
  private static final String PORTFOLIOS = "TradeSim-Portfolio";

  @Autowired private ReservationSweep sweep;
  @Autowired private MongoTemplate mongoTemplate;

  private void heldOverPortfolio(int id, long reservedCents) {
    Portfolio portfolio = new Portfolio(id, id, "Held over", "", 10_000_000, 10_000_000);
    portfolio.setReservedCashCents(reservedCents);
    mongoTemplate.save(portfolio);
  }

  private void heldOverPosition(int portfolioId, String symbol, long reservedQuantity) {
    Position position = new Position(portfolioId, symbol);
    position.setQuantity(500);
    position.setReservedQuantity(reservedQuantity);
    mongoTemplate.save(position);
  }

  private Document stored(int portfolioId) {
    return mongoTemplate
        .getDb()
        .getCollection(PORTFOLIOS)
        .find(new Document("_id", portfolioId))
        .first();
  }

  @Test
  void releasesEveryReservationTheRestartLeftBehind() {
    heldOverPortfolio(101, 250_000);
    heldOverPortfolio(102, 75_000);
    heldOverPosition(101, "AAPL", 40);

    sweep.run();

    assertEquals(0, mongoTemplate.findById(101, Portfolio.class).getReservedCashCents());
    assertEquals(0, mongoTemplate.findById(102, Portfolio.class).getReservedCashCents());
    assertEquals(0, mongoTemplate.findById("101:AAPL", Position.class).getReservedQuantity());
  }

  @Test
  void zeroesTheDollarFieldAlongsideTheCents() {
    heldOverPortfolio(103, 12_345);

    sweep.run();

    assertEquals(0L, stored(103).getLong("reservedCashCents"));
    assertEquals(0.0, stored(103).getDouble("reservedCash"));
  }

  /**
   * A sweep killed partway through leaves some documents cleared and some not. The rerun has to
   * finish the job and leave everything the first run wrote exactly as it found it.
   */
  @Test
  void aSweepThatDiedPartWayIsFinishedByRunningItAgain() {
    heldOverPortfolio(104, 500_000);
    sweep.run();
    Document afterTheFirstRun = stored(104);

    heldOverPortfolio(105, 900_000);
    sweep.run();

    assertEquals(afterTheFirstRun, stored(104));
    assertEquals(0, mongoTemplate.findById(105, Portfolio.class).getReservedCashCents());
  }
}
