package com.adityamehrotra.paper_trader.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document(collection = "Paper Trader-Transaction")
public class Transaction {

  @Id
  @NotNull(message = "Transaction ID cannot be null")
  @Min(value = 1, message = "Transaction ID must be greater than 0")
  private Integer transactionID;

  @NotNull(message = "Portfolio ID cannot be null")
  @Min(value = 1, message = "Portfolio ID must be greater than 0")
  private Integer portfolioID;

  @NotNull(message = "Account ID cannot be null")
  @Min(value = 1, message = "Account ID must be greater than 0")
  private Integer accountID;

  @NotEmpty(message = "Order type cannot be empty")
  @Size(max = 20, message = "Order type must not exceed 20 characters")
  private String orderType;

  @NotEmpty(message = "Security code cannot be empty")
  @Size(max = 20, message = "Security code must not exceed 20 characters")
  private String securityCode;

  @NotEmpty(message = "GMT time cannot be empty")
  private String gmtTime;

  @NotNull(message = "Share amount cannot be null")
  @Min(value = 0, message = "Share amount must be non-negative")
  private Double shareAmount;

  @NotNull(message = "Cash amount cannot be null")
  @Min(value = 0, message = "Cash amount must be non-negative")
  private Double cashAmount;

  @Min(value = 0, message = "Current price must be non-negative")
  private Double currPrice;
}
