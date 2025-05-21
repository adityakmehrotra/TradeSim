package com.adityamehrotra.paper_trader.dto;

import lombok.Getter;
import org.springframework.web.bind.annotation.RequestParam;

@Getter
public class UpdateHoldingRequest {
    private int holdingID;
    private int transactionID;
    private double shares;
    private double price;
    private String action;

    public UpdateHoldingRequest(int holdingID, int transactionID, double shares, double price, String buy) {
        this.holdingID = holdingID;
        this.transactionID = transactionID;
        this.shares = shares;
        this.price = price;
        this.action = buy;
    }
}
