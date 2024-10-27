package com.adityamehrotra.paper_trader.model;

import jakarta.validation.constraints.NotEmpty;
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
  @Id
  @NotEmpty(message = "Code cannot be empty")
  private String code;

  @NotEmpty(message = "Name cannot be empty")
  private String name;

  @NotEmpty(message = "Country cannot be empty")
  private String country;

  @NotEmpty(message = "Exchange cannot be empty")
  private String exchange;

  @NotEmpty(message = "Exchange cannot be empty")
  private String currency;

  @NotEmpty(message = "Exchange cannot be empty")
  private String type;

  @NotEmpty(message = "Exchange cannot be empty")
  private String isin;
}
