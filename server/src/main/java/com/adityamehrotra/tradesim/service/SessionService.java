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
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class SessionService {
  static final double STARTER_CASH = 100000.0;

  private final SessionRepository sessionRepository;
  private final PortfolioRepository portfolioRepository;
  private final PositionRepository positionRepository;
  private final PortfolioService portfolioService;
  private final MarketService marketService;
  private final MongoTemplate mongoTemplate;

  public SessionService(
      SessionRepository sessionRepository,
      PortfolioRepository portfolioRepository,
      PositionRepository positionRepository,
      PortfolioService portfolioService,
      MarketService marketService,
      MongoTemplate mongoTemplate) {
    this.sessionRepository = sessionRepository;
    this.portfolioRepository = portfolioRepository;
    this.positionRepository = positionRepository;
    this.portfolioService = portfolioService;
    this.marketService = marketService;
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
