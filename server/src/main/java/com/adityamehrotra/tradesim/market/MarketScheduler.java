package com.adityamehrotra.tradesim.market;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Drives the market clock. Tests turn this off with tradesim.market.autotick=false and call the
 * market service themselves, so they can assert against a book that is not moving underneath them.
 */
@Component
@ConditionalOnProperty(
    name = "tradesim.market.autotick",
    havingValue = "true",
    matchIfMissing = true)
public class MarketScheduler {
  private final MarketService marketService;

  public MarketScheduler(MarketService marketService) {
    this.marketService = marketService;
  }

  @Scheduled(fixedDelay = 1000)
  public void tick() {
    marketService.tick();
  }

  @Scheduled(fixedDelay = 5000)
  public void closeCandle() {
    marketService.closeCandle();
  }
}
