package com.adityamehrotra.paper_trader.model;

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
  private Double sharesOwned;
  private Double initialCashInvestment;
  private String gmtPurchased;
  private Double initPrice;
}
