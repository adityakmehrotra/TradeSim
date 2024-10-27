package com.adityamehrotra.paper_trader.model;

import jakarta.validation.constraints.NotEmpty;
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
@Document(collection = "Paper Trader- Specific Account")
public class SpecAccount {
  @Id
  @NotEmpty(message = "Username cannot be empty")
  private String username;

  @NotEmpty(message = "Password cannot be empty")
  private String password;

  @NotNull(message = "Account ID cannot be null")
  private Integer accountID;
}
