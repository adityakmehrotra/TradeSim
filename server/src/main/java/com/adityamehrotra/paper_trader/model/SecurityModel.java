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
@Document(collection = "Paper Trader- Security")
public class SecurityModel {
  @Id private String code;

  private String name;

  private String country;

  private String exchange;

  private String currency;

  private String type;

  private String isin;
}
