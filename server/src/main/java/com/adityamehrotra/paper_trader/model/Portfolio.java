package com.adityamehrotra.paper_trader.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
@Document(collection = "Paper Trader- Portfolio")
public class Portfolio {
  @Id
  @NotNull(message = "Portfolio ID cannot be null")
  private Integer portfolioID;

  @NotNull(message = "Account ID cannot be null")
  private Integer accountID;

  @NotNull(message = "Portfolio name cannot be null")
  private String portfolioName;

  @NotNull(message = "Cash amount cannot be null")
  @Min(value = 0, message = "Cash amount must be non-negative")
  private Double cashAmount;

  @NotNull(message = "Initial balance cannot be null")
  @Min(value = 0, message = "Initial balance must be non-negative")
  private Double initialBalance;

  private List<Integer> transactionList;
  private Map<String, Asset> assets;
  private Set<SecurityModel> holdings;
  private Map<String, Double> assetsAvgValue;
}
