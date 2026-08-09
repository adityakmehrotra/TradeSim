package com.adityamehrotra.tradesim.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Money here is whole cents. Dollars as a floating point number cannot represent every cent
 * exactly, so repeated fills drift by fractions of a cent and balances stop adding up; integers
 * cannot drift.
 *
 * <p>The dollar fields are what the previous version read and wrote. They are kept in step with the
 * cents beside them for the compatibility window, so rolling back to that version finds current
 * balances rather than stale ones. Nothing reads them once the cents fields are present.
 */
@Data
@NoArgsConstructor
@ToString
@Getter
@Document(collection = "TradeSim-Portfolio")
public class Portfolio {
  @Id
  @NotNull(message = "Portfolio ID cannot be null")
  @Min(value = 1, message = "Portfolio ID must be greater than 0")
  private Integer portfolioID;

  @NotNull(message = "Account ID cannot be null")
  @Min(value = 1, message = "Account ID must be greater than 0")
  private Integer accountID;

  @Setter
  @NotEmpty(message = "Portfolio name cannot be empty")
  @Size(max = 100, message = "Portfolio name must not exceed 100 characters")
  private String name;

  @Setter private String description;

  private Long cashCents;
  private Long initialBalanceCents;
  private Long reservedCashCents;

  private Double cash;
  private Double initialBalance;
  private Double reservedCash;

  public Portfolio(
      Integer portfolioID,
      Integer accountID,
      String name,
      String description,
      long cashCents,
      long initialBalanceCents) {
    this.portfolioID = portfolioID;
    this.accountID = accountID;
    this.name = name;
    this.description = description;
    setCashCents(cashCents);
    setInitialBalanceCents(initialBalanceCents);
    setReservedCashCents(0);
  }

  public long getCashCents() {
    return cashCents != null ? cashCents : toCents(cash);
  }

  public void setCashCents(long cents) {
    this.cashCents = cents;
    this.cash = toDollars(cents);
  }

  public long getInitialBalanceCents() {
    return initialBalanceCents != null ? initialBalanceCents : toCents(initialBalance);
  }

  public void setInitialBalanceCents(long cents) {
    this.initialBalanceCents = cents;
    this.initialBalance = toDollars(cents);
  }

  public long getReservedCashCents() {
    return reservedCashCents != null ? reservedCashCents : toCents(reservedCash);
  }

  public void setReservedCashCents(long cents) {
    this.reservedCashCents = cents;
    this.reservedCash = toDollars(cents);
  }

  /** Cash that is not already promised to a resting buy order. */
  public long availableCashCents() {
    return getCashCents() - getReservedCashCents();
  }

  /** True when this document predates the cents fields and still needs migrating. */
  public boolean needsCentsBackfill() {
    return cashCents == null || initialBalanceCents == null || reservedCashCents == null;
  }

  private static long toCents(Double dollars) {
    return dollars == null ? 0L : Math.round(dollars * 100);
  }

  private static double toDollars(long cents) {
    return cents / 100.0;
  }
}
