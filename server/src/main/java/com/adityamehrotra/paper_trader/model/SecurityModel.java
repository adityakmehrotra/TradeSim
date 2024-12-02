package com.adityamehrotra.paper_trader.model;

import jakarta.validation.constraints.NotEmpty;
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
@Document(collection = "Paper Trader-Security")
public class SecurityModel {

  @Id
  @NotEmpty(message = "Code cannot be empty")
  @Size(max = 20, message = "Code must not exceed 20 characters")
  @Indexed(unique = true) // Ensure unique codes in the database
  private String code;

  @NotEmpty(message = "Name cannot be empty")
  @Size(max = 100, message = "Name must not exceed 100 characters")
  private String name;

  @NotEmpty(message = "Country cannot be empty")
  @Size(max = 50, message = "Country must not exceed 50 characters")
  private String country;

  @NotEmpty(message = "Exchange cannot be empty")
  @Size(max = 50, message = "Exchange must not exceed 50 characters")
  private String exchange;

  @NotEmpty(message = "Currency cannot be empty")
  @Size(max = 10, message = "Currency must not exceed 10 characters")
  private String currency;

  @NotEmpty(message = "Type cannot be empty")
  @Size(max = 30, message = "Type must not exceed 30 characters")
  private String type;

  @NotEmpty(message = "ISIN cannot be empty")
  @Size(max = 12, message = "ISIN must not exceed 12 characters")
  @Indexed(unique = true) // Ensure unique ISINs in the database
  private String isin;
}
