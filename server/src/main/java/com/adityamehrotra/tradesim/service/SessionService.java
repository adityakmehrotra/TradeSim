package com.adityamehrotra.tradesim.service;

import com.adityamehrotra.tradesim.dto.PortfolioRequest;
import com.adityamehrotra.tradesim.model.Holding;
import com.adityamehrotra.tradesim.model.Portfolio;
import com.adityamehrotra.tradesim.model.Session;
import com.adityamehrotra.tradesim.repository.HoldingRepository;
import com.adityamehrotra.tradesim.repository.PortfolioRepository;
import com.adityamehrotra.tradesim.repository.SessionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class SessionService {
  static final double STARTER_CASH = 100000.0;

  private final SessionRepository sessionRepository;
  private final PortfolioRepository portfolioRepository;
  private final HoldingRepository holdingRepository;
  private final PortfolioService portfolioService;
  private final MongoTemplate mongoTemplate;

  public SessionService(
      SessionRepository sessionRepository,
      PortfolioRepository portfolioRepository,
      HoldingRepository holdingRepository,
      PortfolioService portfolioService,
      MongoTemplate mongoTemplate) {
    this.sessionRepository = sessionRepository;
    this.portfolioRepository = portfolioRepository;
    this.holdingRepository = holdingRepository;
    this.portfolioService = portfolioService;
    this.mongoTemplate = mongoTemplate;
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

    Session session = new Session(UUID.randomUUID().toString(), nextAccountID());
    sessionRepository.save(session);
    seedStarterPortfolio(session.getAccountID());
    return session;
  }

  public List<Portfolio> portfoliosFor(Session session) {
    return portfolioRepository.findByAccountID(session.getAccountID());
  }

  /** Clears the session's portfolios and holdings, then seeds a fresh starter portfolio. */
  public void reset(Session session) {
    for (Portfolio portfolio : portfolioRepository.findByAccountID(session.getAccountID())) {
      for (Holding holding : holdingRepository.findByPortfolioID(portfolio.getPortfolioID())) {
        holdingRepository.delete(holding);
      }
      portfolioRepository.delete(portfolio);
    }
    seedStarterPortfolio(session.getAccountID());
  }

  private void seedStarterPortfolio(int accountID) {
    portfolioService.createPortfolio(
        new PortfolioRequest(
            accountID, "Starter Portfolio", "Virtual starting funds", STARTER_CASH, STARTER_CASH));
  }

  private int nextAccountID() {
    Query query = new Query();
    query.with(Sort.by(Sort.Direction.DESC, "accountID"));
    query.limit(1);

    Session last = mongoTemplate.findOne(query, Session.class);
    return last == null ? 1 : last.getAccountID() + 1;
  }
}
