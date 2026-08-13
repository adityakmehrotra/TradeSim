package com.adityamehrotra.tradesim.service;

import com.adityamehrotra.tradesim.model.Portfolio;
import com.adityamehrotra.tradesim.model.Position;
import com.adityamehrotra.tradesim.repository.PortfolioRepository;
import com.adityamehrotra.tradesim.repository.PositionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

/**
 * Fills in the cents fields on documents written before money became integral.
 *
 * <p>Whether a document needs repair is decided by looking at the document, not by a stored version
 * number. A version record alone would let this skip a document written by an older build after a
 * rollback, which is exactly when the repair is needed. Recording the version is only for
 * reporting.
 *
 * <p>Rewriting an already migrated document is a no-op, so running this repeatedly is safe.
 *
 * <p>{@link com.adityamehrotra.tradesim.startup.StartupReconciliation} decides when this runs and
 * retries it, so a database that is unreachable for a moment at boot does not leave the application
 * serving requests against documents it has not repaired.
 */
@Service
public class CentsMigration {
  public static final String VERSION = "001-cents";

  private static final Logger log = LoggerFactory.getLogger(CentsMigration.class);

  private final PortfolioRepository portfolioRepository;
  private final PositionRepository positionRepository;
  private final MongoTemplate mongoTemplate;

  public CentsMigration(
      PortfolioRepository portfolioRepository,
      PositionRepository positionRepository,
      MongoTemplate mongoTemplate) {
    this.portfolioRepository = portfolioRepository;
    this.positionRepository = positionRepository;
    this.mongoTemplate = mongoTemplate;
  }

  public void migrate() {
    long portfolios = migratePortfolios();
    long positions = migratePositions();

    if (portfolios > 0 || positions > 0) {
      log.info("Cents migration filled in {} portfolios and {} positions", portfolios, positions);
    }
    recordRun(portfolios, positions);
  }

  private long migratePortfolios() {
    long repaired = 0;
    long totalCents = 0;
    for (Portfolio portfolio : portfolioRepository.findAll()) {
      if (portfolio.needsCentsBackfill()) {
        // The getters read the dollar field when cents are missing, so writing the value back
        // through the setter is what stores it as cents and keeps the dollar field in step.
        portfolio.setCashCents(portfolio.getCashCents());
        portfolio.setInitialBalanceCents(portfolio.getInitialBalanceCents());
        portfolio.setReservedCashCents(portfolio.getReservedCashCents());
        portfolioRepository.save(portfolio);
        repaired++;
      }
      totalCents += portfolio.getCashCents();
    }
    if (repaired > 0) {
      log.info("Cash across all portfolios after migrating: {} cents", totalCents);
    }
    return repaired;
  }

  private long migratePositions() {
    long repaired = 0;
    for (Position position : positionRepository.findAll()) {
      if (position.needsCentsBackfill()) {
        position.setRealizedPnlCents(position.getRealizedPnlCents());
        positionRepository.save(position);
        repaired++;
      }
    }
    return repaired;
  }

  private void recordRun(long portfolios, long positions) {
    mongoTemplate.upsert(
        Query.query(Criteria.where("_id").is(VERSION)),
        new Update()
            .set("ranAt", System.currentTimeMillis())
            .inc("portfoliosRepaired", portfolios)
            .inc("positionsRepaired", positions),
        "TradeSim-Migration");
  }
}
