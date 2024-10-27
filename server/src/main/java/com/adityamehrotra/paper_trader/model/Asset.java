package com.adityamehrotra.paper_trader.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document(collection = "Paper Trader- Asset")
public class Asset {
  @NotNull(message = "Shares owned cannot be null")
  @Min(value = 0, message = "Shares owned must be greater than or equal to 0")
  private Double sharesOwned;

  @NotNull(message = "Initial cash investment cannot be null")
  @Min(value = 0, message = "Initial cash investment must be non-negative")
  private Double initialCashInvestment;

  @NotNull(message = "GMT purchased time cannot be null")
  private String gmtPurchased;

  @NotNull(message = "Initial price cannot be null")
  @Min(value = 0, message = "Initial price must be non-negative")
  private Double initPrice;
}
