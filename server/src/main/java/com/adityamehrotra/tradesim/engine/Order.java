package com.adityamehrotra.tradesim.engine;

/**
 * A single order resting in or crossing an {@link OrderBook}. Prices are in whole cents and
 * quantities in whole shares so that matching is exact integer arithmetic. Market orders ignore
 * {@code limitPriceCents}.
 */
public class Order {
  private final long id;
  private final long ownerId;
  private final Side side;
  private final OrderType type;
  private final long limitPriceCents;
  private final long originalQuantity;
  private long remainingQuantity;

  public Order(
      long id, long ownerId, Side side, OrderType type, long limitPriceCents, long quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
    if (type == OrderType.LIMIT && limitPriceCents <= 0) {
      throw new IllegalArgumentException("Limit price must be positive");
    }
    this.id = id;
    this.ownerId = ownerId;
    this.side = side;
    this.type = type;
    this.limitPriceCents = limitPriceCents;
    this.originalQuantity = quantity;
    this.remainingQuantity = quantity;
  }

  void reduce(long quantity) {
    if (quantity > remainingQuantity) {
      throw new IllegalArgumentException("Cannot reduce an order below zero");
    }
    remainingQuantity -= quantity;
  }

  public long getId() {
    return id;
  }

  public long getOwnerId() {
    return ownerId;
  }

  public Side getSide() {
    return side;
  }

  public OrderType getType() {
    return type;
  }

  public long getLimitPriceCents() {
    return limitPriceCents;
  }

  public long getOriginalQuantity() {
    return originalQuantity;
  }

  public long getRemainingQuantity() {
    return remainingQuantity;
  }

  public long getFilledQuantity() {
    return originalQuantity - remainingQuantity;
  }
}
