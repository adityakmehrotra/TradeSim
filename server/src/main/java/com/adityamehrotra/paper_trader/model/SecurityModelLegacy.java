package com.adityamehrotra.paper_trader.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document(collection = "Paper Trader-Security")
public class SecurityModelLegacy {

  @Id
  private String code;

  private String name;

  private String country;

  private String exchange;

  private String currency;

  private String type;
  
  private String isin;
}
