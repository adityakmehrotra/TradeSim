package com.adityamehrotra.tradesim.controller;

import com.adityamehrotra.tradesim.market.MarketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Public market data: the symbol list, quotes, order book depth, the trade tape, and candles. */
@RestController
@RequestMapping("/tradesim/api/market")
public class MarketController {
  private final MarketService marketService;

  public MarketController(MarketService marketService) {
    this.marketService = marketService;
  }

  @GetMapping("/instruments")
  public ResponseEntity<?> instruments() {
    return ResponseEntity.ok(marketService.instruments());
  }

  @GetMapping("/quote")
  public ResponseEntity<?> quote(@RequestParam String symbol) {
    return handle(() -> marketService.quote(symbol));
  }

  @GetMapping("/depth")
  public ResponseEntity<?> depth(@RequestParam String symbol) {
    return handle(() -> marketService.depth(symbol));
  }

  @GetMapping("/trades")
  public ResponseEntity<?> trades(@RequestParam String symbol) {
    return handle(() -> marketService.recentTrades(symbol));
  }

  @GetMapping("/candles")
  public ResponseEntity<?> candles(@RequestParam String symbol) {
    return handle(() -> marketService.candles(symbol));
  }

  private ResponseEntity<?> handle(java.util.function.Supplier<Object> supplier) {
    try {
      return ResponseEntity.ok(supplier.get());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
    }
  }
}
