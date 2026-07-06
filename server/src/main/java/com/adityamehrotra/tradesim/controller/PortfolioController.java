package com.adityamehrotra.tradesim.controller;

import com.adityamehrotra.tradesim.dto.PortfolioRequest;
import com.adityamehrotra.tradesim.model.Portfolio;
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
  public ResponseEntity<?> createPortfolio(@RequestBody PortfolioRequest portfolio) {
    try {
      int portfolioID = portfolioService.createPortfolio(portfolio);
      Map<String, Object> response = new HashMap<>();
      response.put("id", portfolioID);

      return new ResponseEntity<>(response, HttpStatus.CREATED);
    } catch (IllegalArgumentException e) {
      return badRequest(e);
    }
  }

  @GetMapping("/get")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<?> getPortfolio(@RequestParam Integer portfolioID) {
    try {
      Portfolio portfolio = portfolioService.getPortfolio(portfolioID);
      return new ResponseEntity<>(portfolio, HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      return badRequest(e);
    }
  }

  @PutMapping("/name")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public ResponseEntity<?> updatePortfolioName(
      @RequestParam Integer portfolioID, @RequestParam String name) {
    try {
      portfolioService.updatePortfolioName(portfolioID, name);
      Map<String, String> response = new HashMap<>();
      response.put("message", "Portfolio name updated successfully");

      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      return badRequest(e);
    }
  }

  @PutMapping("/description")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public ResponseEntity<?> updatePortfolioDescription(
      @RequestParam Integer portfolioID, @RequestParam String description) {
    try {
      portfolioService.updatePortfolioDescription(portfolioID, description);
      Map<String, String> response = new HashMap<>();
      response.put("message", "Portfolio description updated successfully");

      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      return badRequest(e);
    }
  }

  @DeleteMapping("/delete")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<?> deletePortfolio(@RequestParam Integer portfolioID) {
    try {
      portfolioService.deletePortfolio(portfolioID);
      Map<String, String> response = new HashMap<>();
      response.put("message", "Portfolio deleted successfully");

      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      return badRequest(e);
    }
  }

  private ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("error", e.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }
}
