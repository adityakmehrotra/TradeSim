package com.adityamehrotra.paper_trader.dto;

import lombok.Getter;

@Getter
public class TransactionRequest {
  private Integer portfolioID;
  private Integer holdingID;
  private String ticker;
  private Instant filledDate;
  private Instant submittedDate;
  private String type;
  private String status;
  private Double shares;
  private Double price;
  private Double totalAmount;

  public HoldingRequest(int portfolioID, int holdingID, String ticker, Instant filledDate,
                        Instant submittedDate, String type, String status, Double shares, Double price, Double totalAmount) {
    this.portfolioID = portfolioID;
    this.holdingID = holdingID;
    this.ticker = ticker;
    this.filledDate = filledDate;
    this.submittedDate = submittedDate;
    this.type = type;
    this.status = status;
    this.shares = shares;
    this.price = price;
    this.totalAmount = totalAmount;
  }
}
