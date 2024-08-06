package com.adityamehrotra.paper_trader.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
  @Id private Integer portfolioID;
  private Integer accountID;
  private String portfolioName;
  private Double cashAmount;
  private Double initialBalance;
  private List<Integer> transactionList;
  private Map<String, Asset> assets;
  private Set<SecurityModel> holdings;
  private Map<String, Double> assetsAvgValue;
}
