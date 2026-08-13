package com.adityamehrotra.tradesim.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.adityamehrotra.tradesim.market.Instruments;
import com.adityamehrotra.tradesim.market.MarketService;
import com.adityamehrotra.tradesim.model.Session;
import com.adityamehrotra.tradesim.repository.PortfolioRepository;
import com.adityamehrotra.tradesim.repository.PositionRepository;
import com.adityamehrotra.tradesim.repository.SessionRepository;
import com.adityamehrotra.tradesim.service.PortfolioService;
import com.adityamehrotra.tradesim.startup.SessionCleanup;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Cleanup deletes people's data, so what it does when switched off matters as much as when on. */
class SessionCleanupIntegrationTest extends IntegrationTestBase {
  private static final String SYMBOL = Instruments.ALL.get(0).symbol();
  private static final long THIRTY_ONE_DAYS = Duration.ofDays(31).toMillis();

  @Autowired private SessionRepository sessionRepository;
  @Autowired private PortfolioRepository portfolioRepository;
  @Autowired private PositionRepository positionRepository;
  @Autowired private PortfolioService portfolioService;
  @Autowired private MarketService marketService;

  private SessionCleanup cleanup(boolean enabled, boolean dryRun) {
    return new SessionCleanup(
        sessionRepository, portfolioRepository, portfolioService, enabled, dryRun, 30);
  }

  /**
   * Ages the session past the inactivity window. This has to come after any request the test makes,
   * because using a session marks it active again, which is the point of the stamp.
   */
  private void abandon(TestSession session) {
    String sessionId = session.cookie().substring(session.cookie().indexOf('=') + 1);
    Session stored = sessionRepository.findById(sessionId).orElseThrow();
    stored.setLastActive(System.currentTimeMillis() - THIRTY_ONE_DAYS);
    sessionRepository.save(stored);
  }

  @Test
  void reportsWhatItWouldDeleteWithoutDeletingIt() {
    abandon(openSession());

    SessionCleanup.Report report = cleanup(true, true).sweep();

    assertTrue(report.dryRun());
    assertEquals(1, report.candidateSessions());
    assertEquals(1, report.candidatePortfolios());
    assertEquals(0, report.deletedSessions());
    assertEquals(1, sessionRepository.count(), "a dry run must not delete anything");
    assertEquals(1, portfolioRepository.count());
  }

  @Test
  void deletesTheSessionAndEverythingUnderItOnceEnabled() {
    TestSession session = openSession();
    int portfolioId = session.firstPortfolioId();
    placeOrder(session, marketBuy(portfolioId, SYMBOL, 10));
    placeOrder(session, limitOrder(portfolioId, SYMBOL, "SELL", 9_000_000, 10));
    assertEquals(1, positionRepository.count());
    assertEquals(1, marketService.openOrders(session.accountId()).size());
    abandon(session);

    SessionCleanup.Report report = cleanup(true, false).sweep();

    assertEquals(1, report.deletedSessions());
    assertEquals(1, report.deletedPortfolios());
    assertEquals(0, sessionRepository.count());
    assertEquals(0, portfolioRepository.count());
    assertEquals(0, positionRepository.count(), "positions must go with the portfolio");
    assertTrue(
        marketService.openOrders(session.accountId()).isEmpty(),
        "resting orders must be cancelled, not left pointing at a portfolio that is gone");
  }

  @Test
  void leavesSessionsThatAreStillInUseAlone() {
    openSession();

    SessionCleanup.Report report = cleanup(true, false).sweep();

    assertEquals(0, report.candidateSessions());
    assertEquals(1, sessionRepository.count());
  }

  @Test
  void runningItAgainFindsNothingLeftToDo() {
    abandon(openSession());

    cleanup(true, false).sweep();
    SessionCleanup.Report second = cleanup(true, false).sweep();

    assertEquals(0, second.candidateSessions());
    assertEquals(0, second.deletedSessions());
  }

  @Test
  void doesNothingAtAllWhenTheFlagIsOff() {
    abandon(openSession());

    cleanup(false, false).run();

    assertEquals(1, sessionRepository.count(), "the flag has to gate the scheduled entry point");
  }

  @Test
  void recordsWhenASessionWasLastSeen() {
    openSession();

    Session stored = sessionRepository.findAll().stream().findFirst().orElseThrow();
    assertNotNull(stored.getCreatedAt());
    assertNotNull(stored.getLastActive());
  }

  /** A session written before activity was tracked must not be read as ancient and swept away. */
  @Test
  void treatsSessionsWithNoStampAsCurrent() {
    openSession();
    Session stored = sessionRepository.findAll().stream().findFirst().orElseThrow();
    stored.setLastActive(null);
    stored.setCreatedAt(null);
    sessionRepository.save(stored);

    SessionCleanup.Report report = cleanup(true, false).sweep();

    assertEquals(0, report.candidateSessions());
    assertEquals(1, sessionRepository.count());
    assertNull(sessionRepository.findById(stored.getSessionId()).orElseThrow().getLastActive());
  }
}
