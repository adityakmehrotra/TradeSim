package com.adityamehrotra.paper_trader.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document(collection = "Paper Trader-Portfolio")
public class PortfolioLegacy {

  @Id
  @NotNull(message = "Portfolio ID cannot be null")
  @Min(value = 1, message = "Portfolio ID must be greater than 0")
  private Integer portfolioID;

  @NotNull(message = "Account ID cannot be null")
  @Min(value = 1, message = "Account ID must be greater than 0")
  @Indexed
  private Integer accountID;

  @NotEmpty(message = "Portfolio name cannot be empty")
  @Size(max = 100, message = "Portfolio name must not exceed 100 characters")
  private String portfolioName;

  @NotNull(message = "Cash amount cannot be null")
  @Min(value = 0, message = "Cash amount must be non-negative")
  private Double cashAmount;

  @NotNull(message = "Initial balance cannot be null")
  @Min(value = 0, message = "Initial balance must be non-negative")
  private Double initialBalance;

  @NotNull(message = "Transaction list cannot be null")
  @Size(max = 1000, message = "Transaction list cannot contain more than 1000 transactions")
  private List<@NotNull(message = "Transaction ID cannot be null") @Min(value = 1, message = "Transaction ID must be greater than 0") Integer> transactionList;

  @NotNull(message = "Assets map cannot be null")
  private Map<
          @NotEmpty(message = "Asset key cannot be empty") String,
          @NotNull(message = "Asset object cannot be null") Asset> assets;

  @NotNull(message = "Holdings set cannot be null")
  @Size(max = 500, message = "Holdings set cannot contain more than 500 securities")
  private Set<
          @NotNull(message = "Holding object cannot be null") SecurityModel> holdings;

  @NotNull(message = "Assets average value map cannot be null")
  private Map<
          @NotEmpty(message = "Asset ticker cannot be empty") String,
          @NotNull(message = "Average value cannot be null") @Min(value = 0, message = "Average value must be non-negative") Double> assetsAvgValue;
}
