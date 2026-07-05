package com.adityamehrotra.tradesim.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A portfolio's holding in one symbol, tracked as FIFO lots so realized profit and loss is exact.
 * reservedQuantity is the number of shares committed to resting sell orders.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "TradeSim-Position")
public class Position {
  @Id private String id;

  private Integer portfolioID;
  private String symbol;
  private long quantity;
  private long reservedQuantity;
  private double realizedPnl;
  private List<Lot> lots = new ArrayList<>();

  public Position(Integer portfolioID, String symbol) {
    this.id = portfolioID + ":" + symbol;
    this.portfolioID = portfolioID;
    this.symbol = symbol;
    this.quantity = 0;
    this.reservedQuantity = 0;
    this.realizedPnl = 0.0;
    this.lots = new ArrayList<>();
  }

  public long availableQuantity() {
    return quantity - reservedQuantity;
  }
}
