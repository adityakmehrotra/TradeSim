package com.adityamehrotra.paper_trader.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
@Document(collection = "Paper Trader-SpecificAccount")
public class SpecAccount {

  @Id
  @NotEmpty(message = "Username cannot be empty")
  @Size(max = 50, message = "Username must not exceed 50 characters")
  @Indexed(unique = true) // Ensure usernames are unique in the database
  private String username;

  @NotEmpty(message = "Password cannot be empty")
  @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
  private String password;

  @NotNull(message = "Account ID cannot be null")
  private Integer accountID;
}
