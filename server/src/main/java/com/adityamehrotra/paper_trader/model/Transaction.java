package com.adityamehrotra.paper_trader.model;

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
@Document(collection = "Paper Trader- Transaction")
public class Transaction {
  @Id private Integer transactionID;
  private Integer portfolioID;
  private Integer accountID;
  private String orderType;
  private String gmtTime;
  private String securityCode;
  private Double shareAmount;
  private Double cashAmount;
  private Double currPrice;
}
