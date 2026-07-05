package com.adityamehrotra.tradesim.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class OrderBookTest {
  private long sequence = 0;

  private Order limit(long owner, Side side, long priceCents, long quantity) {
    return new Order(++sequence, owner, side, OrderType.LIMIT, priceCents, quantity);
  }

  private Order market(long owner, Side side, long quantity) {
    return new Order(++sequence, owner, side, OrderType.MARKET, 0, quantity);
  }

  @Test
  void aggressorTradesAtThePassivePrice() {
    OrderBook book = new OrderBook("NOVA");
    Order resting = limit(1, Side.SELL, 1000, 50);
    book.submit(resting);

    List<Trade> trades = book.submit(limit(2, Side.BUY, 1050, 30));

    assertEquals(1, trades.size());
    assertEquals(1000, trades.get(0).getPriceCents());
    assertEquals(30, trades.get(0).getQuantity());
  }

  @Test
  void betterPriceThenEarlierOrderFillsFirst() {
    OrderBook book = new OrderBook("NOVA");
    book.submit(limit(1, Side.SELL, 1100, 10));
    Order earlyBest = limit(1, Side.SELL, 1000, 10);
    Order lateBest = limit(1, Side.SELL, 1000, 10);
    book.submit(earlyBest);
    book.submit(lateBest);

    List<Trade> trades = book.submit(market(2, Side.BUY, 15));

    assertEquals(1000, trades.get(0).getPriceCents());
    assertEquals(earlyBest.getId(), trades.get(0).getSellOrderId());
    assertEquals(lateBest.getId(), trades.get(1).getSellOrderId());
  }

  @Test
  void unfilledLimitRemainderRests() {
    OrderBook book = new OrderBook("NOVA");
    book.submit(limit(1, Side.SELL, 1000, 10));

    Order aggressive = limit(2, Side.BUY, 1000, 25);
    book.submit(aggressive);

    assertEquals(10, aggressive.getFilledQuantity());
    assertEquals(15, aggressive.getRemainingQuantity());
    assertEquals(1000, book.bestBidCents());
  }

  @Test
  void marketOrderDropsWhatItCannotFill() {
    OrderBook book = new OrderBook("NOVA");
    book.submit(limit(1, Side.SELL, 1000, 10));

    Order sweep = market(2, Side.BUY, 40);
    book.submit(sweep);

    assertEquals(10, sweep.getFilledQuantity());
    assertEquals(30, sweep.getRemainingQuantity());
    assertNull(book.bestAskCents());
    assertNull(book.bestBidCents());
  }

  @Test
  void cancelRemovesARestingOrder() {
    OrderBook book = new OrderBook("NOVA");
    Order resting = limit(1, Side.BUY, 900, 40);
    book.submit(resting);

    assertTrue(book.cancel(resting.getId()));
    assertNull(book.bestBidCents());
    assertEquals(0, book.submit(limit(2, Side.SELL, 900, 10)).size());
  }

  @Test
  void aPartiallyFilledOrderCanStillBeCancelled() {
    OrderBook book = new OrderBook("NOVA");
    Order resting = limit(1, Side.BUY, 900, 40);
    book.submit(resting);
    book.submit(limit(2, Side.SELL, 900, 15));

    assertEquals(25, resting.getRemainingQuantity());
    assertTrue(book.cancel(resting.getId()));
    assertNull(book.bestBidCents());
  }

  @Test
  void matchingNeverLeavesACrossedBook() {
    OrderBook book = new OrderBook("NOVA");
    book.submit(limit(1, Side.BUY, 900, 40));
    book.submit(limit(2, Side.SELL, 1100, 40));
    book.submit(limit(3, Side.SELL, 900, 25));

    Long bestBid = book.bestBidCents();
    Long bestAsk = book.bestAskCents();
    assertTrue(bestBid == null || bestAsk == null || bestBid < bestAsk);
  }

  @Test
  void sharesAreConservedAcrossAMatch() {
    OrderBook book = new OrderBook("NOVA");
    book.submit(limit(1, Side.SELL, 1000, 30));
    book.submit(limit(1, Side.SELL, 1010, 30));

    List<Trade> trades = book.submit(market(2, Side.BUY, 45));

    long traded = trades.stream().mapToLong(Trade::getQuantity).sum();
    assertEquals(45, traded);
  }

  @Test
  void orderRejectsNonPositiveQuantityAndPrice() {
    assertThrows(
        IllegalArgumentException.class, () -> new Order(1, 1, Side.BUY, OrderType.LIMIT, 1000, 0));
    assertThrows(
        IllegalArgumentException.class, () -> new Order(1, 1, Side.BUY, OrderType.LIMIT, 0, 10));
  }
}
