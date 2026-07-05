package com.adityamehrotra.tradesim.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * A central limit order book for one symbol. Orders match by price first, then by arrival time
 * within a price level. Bids are held highest price first, asks lowest price first, and each price
 * level is a FIFO queue. The book holds no external dependencies, so its behavior is exact and unit
 * testable.
 */
public class OrderBook {
  private final String symbol;
  private final NavigableMap<Long, Deque<Order>> bids = new TreeMap<>(Collections.reverseOrder());
  private final NavigableMap<Long, Deque<Order>> asks = new TreeMap<>();
  private final Map<Long, Order> restingById = new HashMap<>();

  public OrderBook(String symbol) {
    this.symbol = symbol;
  }

  public String getSymbol() {
    return symbol;
  }

  /**
   * Matches an incoming order against the resting side, returning the trades it generated. A limit
   * order that is not fully filled rests in the book; an unfilled market order is simply dropped.
   */
  public List<Trade> submit(Order incoming) {
    List<Trade> trades = new ArrayList<>();
    NavigableMap<Long, Deque<Order>> opposite = incoming.getSide() == Side.BUY ? asks : bids;

    while (incoming.getRemainingQuantity() > 0 && !opposite.isEmpty()) {
      Map.Entry<Long, Deque<Order>> best = opposite.firstEntry();
      long restingPrice = best.getKey();
      if (!crosses(incoming, restingPrice)) {
        break;
      }

      Deque<Order> level = best.getValue();
      while (incoming.getRemainingQuantity() > 0 && !level.isEmpty()) {
        Order resting = level.peekFirst();
        long quantity = Math.min(incoming.getRemainingQuantity(), resting.getRemainingQuantity());

        incoming.reduce(quantity);
        resting.reduce(quantity);
        trades.add(buildTrade(incoming, resting, restingPrice, quantity));

        if (resting.getRemainingQuantity() == 0) {
          level.pollFirst();
          restingById.remove(resting.getId());
        }
      }

      if (level.isEmpty()) {
        opposite.pollFirstEntry();
      }
    }

    if (incoming.getType() == OrderType.LIMIT && incoming.getRemainingQuantity() > 0) {
      rest(incoming);
    }
    return trades;
  }

  public boolean cancel(long orderId) {
    Order order = restingById.remove(orderId);
    if (order == null) {
      return false;
    }
    NavigableMap<Long, Deque<Order>> side = order.getSide() == Side.BUY ? bids : asks;
    Deque<Order> level = side.get(order.getLimitPriceCents());
    if (level != null) {
      level.remove(order);
      if (level.isEmpty()) {
        side.remove(order.getLimitPriceCents());
      }
    }
    return true;
  }

  public Long bestBidCents() {
    return bids.isEmpty() ? null : bids.firstKey();
  }

  public Long bestAskCents() {
    return asks.isEmpty() ? null : asks.firstKey();
  }

  public List<BookLevel> bidLevels(int depth) {
    return levels(bids, depth);
  }

  public List<BookLevel> askLevels(int depth) {
    return levels(asks, depth);
  }

  private boolean crosses(Order incoming, long restingPrice) {
    if (incoming.getType() == OrderType.MARKET) {
      return true;
    }
    return incoming.getSide() == Side.BUY
        ? incoming.getLimitPriceCents() >= restingPrice
        : incoming.getLimitPriceCents() <= restingPrice;
  }

  private Trade buildTrade(Order incoming, Order resting, long priceCents, long quantity) {
    long buyOrderId = incoming.getSide() == Side.BUY ? incoming.getId() : resting.getId();
    long sellOrderId = incoming.getSide() == Side.BUY ? resting.getId() : incoming.getId();
    return new Trade(symbol, priceCents, quantity, buyOrderId, sellOrderId, incoming.getSide());
  }

  private void rest(Order order) {
    NavigableMap<Long, Deque<Order>> side = order.getSide() == Side.BUY ? bids : asks;
    side.computeIfAbsent(order.getLimitPriceCents(), price -> new ArrayDeque<>()).addLast(order);
    restingById.put(order.getId(), order);
  }

  private List<BookLevel> levels(NavigableMap<Long, Deque<Order>> side, int depth) {
    List<BookLevel> result = new ArrayList<>();
    for (Map.Entry<Long, Deque<Order>> entry : side.entrySet()) {
      if (result.size() >= depth) {
        break;
      }
      long total = 0;
      for (Order order : entry.getValue()) {
        total += order.getRemainingQuantity();
      }
      result.add(new BookLevel(entry.getKey(), total));
    }
    return result;
  }
}
