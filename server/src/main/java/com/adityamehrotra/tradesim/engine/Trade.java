package com.adityamehrotra.tradesim.engine;

/** An executed match between a resting order and an incoming order. */
public class Trade {
  private final String symbol;
  private final long priceCents;
  private final long quantity;
  private final long buyOrderId;
  private final long sellOrderId;
  private final Side aggressor;

  public Trade(
      String symbol,
      long priceCents,
      long quantity,
      long buyOrderId,
      long sellOrderId,
      Side aggressor) {
    this.symbol = symbol;
    this.priceCents = priceCents;
    this.quantity = quantity;
    this.buyOrderId = buyOrderId;
    this.sellOrderId = sellOrderId;
    this.aggressor = aggressor;
  }

  public String getSymbol() {
    return symbol;
  }

  public long getPriceCents() {
    return priceCents;
  }

  public long getQuantity() {
    return quantity;
  }

  public long getBuyOrderId() {
    return buyOrderId;
  }

  public long getSellOrderId() {
    return sellOrderId;
  }

  public Side getAggressor() {
    return aggressor;
  }
}
