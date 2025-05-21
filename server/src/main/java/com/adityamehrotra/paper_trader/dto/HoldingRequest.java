package com.adityamehrotra.paper_trader.dto;

import lombok.Getter;

@Getter
public class HoldingRequest {
    private Integer holdingID;
    private Integer portfolioID;
    private Integer transactionID;
    private Double shares;
    private Double price;

    public HoldingRequest(int holdingID, int portfolioID, int transactionID, Double shares, Double price) {
        this.holdingID = holdingID;
        this.portfolioID = portfolioID;
        this.transactionID = transactionID;
        this.shares = shares;
        this.price = price;
    }
}
