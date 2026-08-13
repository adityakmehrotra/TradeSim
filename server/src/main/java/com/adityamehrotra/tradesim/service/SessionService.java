package com.adityamehrotra.tradesim.service;

import com.adityamehrotra.tradesim.dto.PortfolioRequest;
import com.adityamehrotra.tradesim.market.MarketService;
import com.adityamehrotra.tradesim.model.Portfolio;
import com.adityamehrotra.tradesim.model.Session;
import com.adityamehrotra.tradesim.repository.PortfolioRepository;
import com.adityamehrotra.tradesim.repository.PositionRepository;
import com.adityamehrotra.tradesim.repository.SessionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SessionService {
  public static final String COOKIE_NAME = "tradesim_session";

  static final long TOUCH_INTERVAL_MS = 3_600_000;

  static final long STARTER_CASH_CENTS = 10_000_000;

  private final SessionRepository sessionRepository;
  private final PortfolioRepository portfolioRepository;
  private final PositionRepository positionRepository;
  private final PortfolioService portfolioService;
  private final MarketService marketService;
  private final SequenceService sequenceService;

  public SessionService(
      SessionRepository sessionRepository,
      PortfolioRepository portfolioRepository,
      PositionRepository positionRepository,
      PortfolioService portfolioService,
      MarketService marketService,
      SequenceService sequenceService) {
    this.sessionRepository = sessionRepository;
    this.portfolioRepository = portfolioRepository;
    this.positionRepository = positionRepository;
    this.portfolioService = portfolioService;
    this.marketService = marketService;
    this.sequenceService = sequenceService;
  }

  /**
   * Looks up an established session, returning null when the caller has no usable cookie. Only the
   * session endpoint creates sessions, because only it can hand the new cookie back.
   */
  public Session find(String token) {
    if (token == null) {
      return null;
    }
    Session session = sessionRepository.findById(token).orElse(null);
    if (session != null) {
      touch(session);
    }
    return session;
  }

  /**
   * Records that the session is still in use, but only once an hour. Writing on every request would
   * turn each poll into a database write, and the cleanup window is measured in days, so an hour of
   * staleness costs nothing.
   */
  private void touch(Session session) {
    long now = System.currentTimeMillis();
    if (now - session.lastActiveOr(0L) < TOUCH_INTERVAL_MS) {
      return;
    }
    session.setLastActive(now);
    sessionRepository.save(session);
  }

  /**
   * Returns the session for the given cookie token, creating a fresh one with a starter portfolio.
   */
  public Session getOrCreate(String token) {
    if (token != null) {
      Session existing = sessionRepository.findById(token).orElse(null);
      if (existing != null) {
        return existing;
      }
    }

    Session session =
        new Session(UUID.randomUUID().toString(), nextAccountID(), System.currentTimeMillis());
    sessionRepository.save(session);
    seedStarterPortfolio(session.getAccountID());
    return session;
  }

  public List<Portfolio> portfoliosFor(Session session) {
    return portfolioRepository.findByAccountID(session.getAccountID());
  }

  /**
   * Wipes the session's trading state and seeds a fresh starter portfolio. Open orders are
   * cancelled first; portfolio ids can be reused after deletion, so leftover orders or positions
   * would bleed into the next portfolio.
   */
  public void reset(Session session) {
    marketService.cancelAllForAccount(session.getAccountID());
    for (Portfolio portfolio : portfolioRepository.findByAccountID(session.getAccountID())) {
      positionRepository.deleteAll(
          positionRepository.findByPortfolioID(portfolio.getPortfolioID()));
      portfolioRepository.delete(portfolio);
    }
    seedStarterPortfolio(session.getAccountID());
  }

  private void seedStarterPortfolio(int accountID) {
    portfolioService.createPortfolio(
        new PortfolioRequest(
            accountID,
            "Starter Portfolio",
            "Virtual starting funds",
            STARTER_CASH_CENTS,
            STARTER_CASH_CENTS));
  }

  private int nextAccountID() {
    return sequenceService.next(SequenceService.ACCOUNT_ID);
  }
}
