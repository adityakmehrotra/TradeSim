package com.adityamehrotra.paper_trader.dto;

public class PortfolioRequest {
    private Integer accountID;
    private String name;
    private String description;
    private Double cash;
    private final Double initialBalance;

    public PortfolioRequest(Integer accountID, String name, String description, Double cash, Double initialBalance) {
        this.accountID = accountID;
        this.name = name;
        this.description = description;
        this.cash = cash;
        this.initialBalance = initialBalance;
    }

    public Integer getAccountID() {
        return accountID;
    }

    public void setAccountID(Integer accountID) {
        this.accountID = accountID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getCash() {
        return cash;
    }

    public void setCash(Double cash) {
        this.cash = cash;
    }

    public Double getInitialBalance() {
        return initialBalance;
    }
}
