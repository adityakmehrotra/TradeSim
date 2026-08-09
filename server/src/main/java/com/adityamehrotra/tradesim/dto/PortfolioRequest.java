package com.adityamehrotra.tradesim.dto;

/**
 * Amounts arrive as whole cents. The dollar fields are still accepted so a client that has not been
 * updated keeps working during the compatibility window; they are only read when cents are absent.
 */
public class PortfolioRequest {
  private Integer accountID;
  private String name;
  private String description;
  private Long cashCents;
  private Long initialBalanceCents;
  private Double cash;
  private Double initialBalance;

  public PortfolioRequest() {}

  public PortfolioRequest(
      Integer accountID,
      String name,
      String description,
      long cashCents,
      long initialBalanceCents) {
    this.accountID = accountID;
    this.name = name;
    this.description = description;
    this.cashCents = cashCents;
    this.initialBalanceCents = initialBalanceCents;
  }

  public long cashCents() {
    return cashCents != null ? cashCents : toCents(cash);
  }

  public long initialBalanceCents() {
    return initialBalanceCents != null ? initialBalanceCents : toCents(initialBalance);
  }

  private static long toCents(Double dollars) {
    return dollars == null ? 0L : Math.round(dollars * 100);
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

  public void setCashCents(Long cashCents) {
    this.cashCents = cashCents;
  }

  public void setInitialBalanceCents(Long initialBalanceCents) {
    this.initialBalanceCents = initialBalanceCents;
  }

  public void setCash(Double cash) {
    this.cash = cash;
  }

  public void setInitialBalance(Double initialBalance) {
    this.initialBalance = initialBalance;
  }
}
