package com.adityamehrotra.paper_trader.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@CrossOrigin
@RequestMapping("/paper_trader/polygon")
@RestController
public class PolygonAPIController {

  @Value("${apiKey}")
  private String polygonApiKey;

  private final RestTemplate restTemplate;

  public PolygonAPIController(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @GetMapping("/price")
  public ResponseEntity<String> getCurrentPrice(@RequestParam String ticker) {
    String url =
            String.format(
                    "https://api.polygon.io/v2/snapshot/locale/us/markets/stocks/tickers/%s?apiKey=%s",
                    ticker, polygonApiKey);
    try {
      ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
      return ResponseEntity.ok(response.getBody());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error fetching current price: " + e.getMessage());
    }
  }

  @GetMapping("/news")
  public ResponseEntity<String> getCurrentNews(@RequestParam String ticker) {
    String url =
            String.format(
                    "https://api.polygon.io/v2/reference/news?ticker=%s&order=desc&limit=50&apiKey=%s",
                    ticker, polygonApiKey);
    try {
      ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
      return ResponseEntity.ok(response.getBody());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error fetching current price: " + e.getMessage());
    }
  }

  @GetMapping("/keyStatistics")
  public ResponseEntity<String> getKeyStatistics(@RequestParam String ticker) {
    String url =
            String.format(
                    "https://api.polygon.io/v2/aggs/ticker/%s/prev?adjusted=true&apiKey=%s",
                    ticker, polygonApiKey);
    try {
      ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
      return ResponseEntity.ok(response.getBody());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error fetching current price: " + e.getMessage());
    }
  }

  @GetMapping("/companyData")
  public ResponseEntity<String> getCompanyData(@RequestParam String ticker) {
    String url =
            String.format(
                    "https://api.polygon.io/v3/reference/tickers/%s?apiKey=%s", ticker, polygonApiKey);
    try {
      ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
      return ResponseEntity.ok(response.getBody());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error fetching current price: " + e.getMessage());
    }
  }

  @GetMapping("/chart")
  public ResponseEntity<String> getChart(
          @RequestParam String ticker,
          @RequestParam Integer multiplier,
          @RequestParam String timespan,
          @RequestParam String from,
          @RequestParam String to) {
    String url =
            String.format(
                    "https://api.polygon.io/v2/aggs/ticker/%s/range/%d/%s/%s/%s?adjusted=true&sort=asc&limit=50000&apiKey=%s",
                    ticker, multiplier, timespan, from, to, polygonApiKey);
    try {
      ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
      return ResponseEntity.ok(response.getBody());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error fetching current price: " + e.getMessage());
    }
  }
}
