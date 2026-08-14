package com.adityamehrotra.tradesim.startup;

import com.adityamehrotra.tradesim.model.Portfolio;
import com.adityamehrotra.tradesim.model.Session;
import com.adityamehrotra.tradesim.repository.PortfolioRepository;
import com.adityamehrotra.tradesim.repository.SessionRepository;
import com.adityamehrotra.tradesim.service.PortfolioService;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Removes sessions nobody has used in a long time, along with everything hanging off them.
 *
 * <p>This deletes people's data, so it is off unless switched on, and reports what it would delete
 * without deleting it until that is switched off too. Read the report first, then enable deletion.
 *
 * <p>A TTL index on the session would be simpler and wrong: Mongo would drop the session document
 * and leave its portfolios, positions, and resting orders behind with nothing pointing at them.
 * Deletion has to go through the same cascade a portfolio delete uses.
 */
@Component
public class SessionCleanup {
  private static final Logger log = LoggerFactory.getLogger(SessionCleanup.class);

  private final SessionRepository sessionRepository;
  private final PortfolioRepository portfolioRepository;
  private final PortfolioService portfolioService;
  private final boolean enabled;
  private final boolean dryRun;
  private final long inactiveMillis;

  public SessionCleanup(
      SessionRepository sessionRepository,
      PortfolioRepository portfolioRepository,
      PortfolioService portfolioService,
      @Value("${tradesim.cleanup.enabled:false}") boolean enabled,
      @Value("${tradesim.cleanup.dry-run:true}") boolean dryRun,
      @Value("${tradesim.cleanup.inactive-days:30}") long inactiveDays) {
    this.sessionRepository = sessionRepository;
    this.portfolioRepository = portfolioRepository;
    this.portfolioService = portfolioService;
    this.enabled = enabled;
    this.dryRun = dryRun;
    this.inactiveMillis = Duration.ofDays(inactiveDays).toMillis();
  }

  @Scheduled(
      initialDelayString = "${tradesim.cleanup.initial-delay-ms:60000}",
      fixedDelayString = "${tradesim.cleanup.interval-ms:86400000}")
  public void run() {
    if (!enabled) {
      return;
    }
    sweep();
  }

  /** Returns what it found, so a test can assert on it and an operator can read it in the log. */
  public Report sweep() {
    long now = System.currentTimeMillis();
    long cutoff = now - inactiveMillis;
    List<Session> abandoned =
        sessionRepository.findAll().stream()
            .filter(session -> session.lastActiveOr(now) < cutoff)
            .toList();

    long portfolios = 0;
    for (Session session : abandoned) {
      portfolios += portfolioRepository.findByAccountID(session.getAccountID()).size();
    }

    if (dryRun) {
      log.info(
          "Session cleanup dry run: {} sessions and {} portfolios are past {} days of inactivity."
              + " Nothing was deleted. Set tradesim.cleanup.dry-run=false to delete them.",
          abandoned.size(),
          portfolios,
          Duration.ofMillis(inactiveMillis).toDays());
      return new Report(abandoned.size(), portfolios, 0, 0, true);
    }

    long deletedSessions = 0;
    long deletedPortfolios = 0;
    long failures = 0;
    for (Session session : abandoned) {
      try {
        // The portfolio cascade cancels resting orders and clears positions before the document
        // goes, so nothing is left in the book pointing at an account that no longer exists.
        for (Portfolio portfolio : portfolioRepository.findByAccountID(session.getAccountID())) {
          portfolioService.deleteOwned(portfolio);
          deletedPortfolios++;
        }
        sessionRepository.delete(session);
        deletedSessions++;
      } catch (RuntimeException e) {
        // Whatever is left keeps its session, so the next run finds it again and retries.
        failures++;
        log.warn("Could not clean up session {}: {}", session.getSessionId(), e.toString());
      }
    }

    log.info(
        "Session cleanup removed {} sessions and {} portfolios, with {} failures",
        deletedSessions,
        deletedPortfolios,
        failures);
    return new Report(abandoned.size(), portfolios, deletedSessions, deletedPortfolios, false);
  }

  public record Report(
      long candidateSessions,
      long candidatePortfolios,
      long deletedSessions,
      long deletedPortfolios,
      boolean dryRun) {}
}
