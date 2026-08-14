package com.adityamehrotra.tradesim.controller;

import com.adityamehrotra.tradesim.dto.PortfolioRequest;
import com.adityamehrotra.tradesim.exception.RateLimitedException;
import com.adityamehrotra.tradesim.model.Portfolio;
import com.adityamehrotra.tradesim.model.Session;
import com.adityamehrotra.tradesim.service.PortfolioService;
import com.adityamehrotra.tradesim.web.RateLimiter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tradesim/api/portfolio")
@Validated
public class PortfolioController {
  private final PortfolioService portfolioService;
  private final RateLimiter rateLimiter;
  private final int changesPerMinute;

  public PortfolioController(
      PortfolioService portfolioService,
      RateLimiter rateLimiter,
      @Value("${tradesim.limits.portfolio-changes-per-minute:20}") int changesPerMinute) {
    this.portfolioService = portfolioService;
    this.rateLimiter = rateLimiter;
    this.changesPerMinute = changesPerMinute;
  }

  /** Reads are cheap and self limiting. Only the routes that write are counted. */
  private void requireChangeAllowance(Session session) {
    if (!rateLimiter.allow("portfolio:" + session.getAccountID(), changesPerMinute, 60_000L)) {
      throw new RateLimitedException("Too many portfolio changes. Try again shortly.");
    }
  }

  @PostMapping("/create")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<?> createPortfolio(Session session, @RequestBody PortfolioRequest request) {
    requireChangeAllowance(session);
    // Whatever account the body claims is ignored. A session only ever creates its own portfolios.
    request.setAccountID(session.getAccountID());
    int portfolioID = portfolioService.createPortfolio(request);

    Map<String, Object> response = new HashMap<>();
    response.put("id", portfolioID);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("/get")
  public ResponseEntity<?> getPortfolio(Session session, @RequestParam Integer portfolioID) {
    Portfolio portfolio = portfolioService.requireOwned(portfolioID, session.getAccountID());
    return new ResponseEntity<>(portfolio, HttpStatus.OK);
  }

  @PutMapping("/name")
  public ResponseEntity<?> updatePortfolioName(
      Session session, @RequestParam Integer portfolioID, @RequestParam String name) {
    requireChangeAllowance(session);
    portfolioService.updatePortfolioName(portfolioID, session.getAccountID(), name);
    return ResponseEntity.ok(Map.of("message", "Portfolio name updated successfully"));
  }

  @PutMapping("/description")
  public ResponseEntity<?> updatePortfolioDescription(
      Session session, @RequestParam Integer portfolioID, @RequestParam String description) {
    requireChangeAllowance(session);
    portfolioService.updatePortfolioDescription(portfolioID, session.getAccountID(), description);
    return ResponseEntity.ok(Map.of("message", "Portfolio description updated successfully"));
  }

  @DeleteMapping("/delete")
  public ResponseEntity<?> deletePortfolio(Session session, @RequestParam Integer portfolioID) {
    requireChangeAllowance(session);
    portfolioService.deletePortfolio(portfolioID, session.getAccountID());
    return ResponseEntity.ok(Map.of("message", "Portfolio deleted successfully"));
  }
}
