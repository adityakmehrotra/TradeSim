package com.adityamehrotra.tradesim.controller;

import com.adityamehrotra.tradesim.dto.PortfolioRequest;
import com.adityamehrotra.tradesim.model.Portfolio;
import com.adityamehrotra.tradesim.model.Session;
import com.adityamehrotra.tradesim.service.PortfolioService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tradesim/api/portfolio")
@Validated
public class PortfolioController {
  private final PortfolioService portfolioService;

  public PortfolioController(PortfolioService portfolioService) {
    this.portfolioService = portfolioService;
  }

  @PostMapping("/create")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<?> createPortfolio(Session session, @RequestBody PortfolioRequest request) {
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
    portfolioService.updatePortfolioName(portfolioID, session.getAccountID(), name);
    return ResponseEntity.ok(Map.of("message", "Portfolio name updated successfully"));
  }

  @PutMapping("/description")
  public ResponseEntity<?> updatePortfolioDescription(
      Session session, @RequestParam Integer portfolioID, @RequestParam String description) {
    portfolioService.updatePortfolioDescription(portfolioID, session.getAccountID(), description);
    return ResponseEntity.ok(Map.of("message", "Portfolio description updated successfully"));
  }

  @DeleteMapping("/delete")
  public ResponseEntity<?> deletePortfolio(Session session, @RequestParam Integer portfolioID) {
    portfolioService.deletePortfolio(portfolioID, session.getAccountID());
    return ResponseEntity.ok(Map.of("message", "Portfolio deleted successfully"));
  }
}
