package com.adityamehrotra.tradesim.market;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.adityamehrotra.tradesim.service.PositionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** The market clock runs for as long as the process does, so where it wanders has to be bounded. */
@ExtendWith(MockitoExtension.class)
class MarketServiceTickTest {
  private static final int SIX_HOURS_OF_TICKS = 6 * 3600;

  @Mock private PositionService positionService;

  @Test
  void keepsEveryPriceNearItsSeedOverHoursOfTicks() {
    MarketService marketService = new MarketService(positionService);
    marketService.seed();

    for (int i = 0; i < SIX_HOURS_OF_TICKS; i++) {
      marketService.tick();
    }

    for (Instrument instrument : Instruments.ALL) {
      long last = marketService.lastPrice(instrument.symbol());
      long seed = instrument.referencePriceCents();
      assertTrue(
          last > seed / 2 && last < seed * 2,
          instrument.symbol() + " started at " + seed + " and ended at " + last);
    }
  }
}
