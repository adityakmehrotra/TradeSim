package com.adityamehrotra.tradesim.market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.adityamehrotra.tradesim.engine.OrderType;
import com.adityamehrotra.tradesim.engine.Side;
import com.adityamehrotra.tradesim.service.PositionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Covers the sweep that session reset uses to clear an account's resting orders. */
@ExtendWith(MockitoExtension.class)
class MarketServiceSweepTest {
  private static final long FAR_BELOW_MARKET = 1;
  private static final long QUANTITY = 10;

  @Mock private PositionService positionService;

  private MarketService marketService;
  private String symbol;

  @BeforeEach
  void setUp() {
    marketService = new MarketService(positionService);
    marketService.seed();
    symbol = Instruments.ALL.get(0).symbol();
  }

  /** A buy priced far under the book never crosses, so it stays resting for the sweep to find. */
  private void restingBuy(int portfolioId, int accountId) {
    when(positionService.reserveCash(anyInt(), anyLong())).thenReturn(true);
    var result =
        marketService.placeOrder(
            portfolioId, accountId, symbol, Side.BUY, OrderType.LIMIT, FAR_BELOW_MARKET, QUANTITY);
    assertTrue(result.resting());
  }

  @Test
  void cancelsEveryRestingOrderTheAccountHas() {
    restingBuy(1, 1);
    restingBuy(1, 1);
    assertEquals(2, marketService.openOrders(1).size());

    marketService.cancelAllForAccount(1);

    assertEquals(0, marketService.openOrders(1).size());
  }

  @Test
  void leavesOtherAccountsOrdersAlone() {
    restingBuy(1, 1);
    restingBuy(2, 2);

    marketService.cancelAllForAccount(1);

    assertEquals(0, marketService.openOrders(1).size());
    assertEquals(1, marketService.openOrders(2).size());
  }

  @Test
  void releasesTheCashThatCancelledOrdersHeld() {
    restingBuy(1, 1);

    marketService.cancelAllForAccount(1);

    verify(positionService).releaseCash(1, FAR_BELOW_MARKET * QUANTITY);
  }
}
