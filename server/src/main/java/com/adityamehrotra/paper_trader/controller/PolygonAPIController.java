package com.adityamehrotra.paper_trader.controller;

import com.adityamehrotra.paper_trader.service.PolygonAPIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RequestMapping("/paper_trader/polygon")
@RestController
public class PolygonAPIController {

  private final PolygonAPIService polygonAPIService;

  public PolygonAPIController(PolygonAPIService polygonAPIService) {
    this.polygonAPIService = polygonAPIService;
  }

  @GetMapping("/price")
  public ResponseEntity<String> getCurrentPrice(@RequestParam String ticker) {
    try {
      return polygonAPIService.getCurrentPrice(ticker);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error fetching current price: " + e.getMessage());
    }
  }

  @GetMapping("/news")
  public ResponseEntity<String> getCurrentNews(@RequestParam String ticker) {
    try {
      return polygonAPIService.getCurrentNews(ticker);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error fetching current news: " + e.getMessage());
    }
  }

  @GetMapping("/keyStatistics")
  public ResponseEntity<String> getKeyStatistics(@RequestParam String ticker) {
    try {
      return polygonAPIService.getKeyStatistics(ticker);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error fetching key statistics: " + e.getMessage());
    }
  }

  @GetMapping("/companyData")
  public ResponseEntity<String> getCompanyData(@RequestParam String ticker) {
    try {
      return polygonAPIService.getCompanyData(ticker);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error fetching company data: " + e.getMessage());
    }
  }

  @GetMapping("/chart")
  public ResponseEntity<String> getChart(
          @RequestParam String ticker,
          @RequestParam Integer multiplier,
          @RequestParam String timespan,
          @RequestParam String from,
          @RequestParam String to) {
    try {
      return polygonAPIService.getChart(ticker, multiplier, timespan, from, to);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error fetching chart data: " + e.getMessage());
    }
  }
}