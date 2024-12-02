package com.adityamehrotra.paper_trader.model;

import java.util.List;

import jakarta.validation.constraints.Email;
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
@Document(collection = "Paper Trader-Account")
public class Account {

  @Id
  @NotNull(message = "Account ID cannot be null")
  private Integer accountID;

  @NotEmpty(message = "First name cannot be empty")
  @Size(max = 50, message = "First name cannot exceed 50 characters")
  private String firstName;

  @NotEmpty(message = "Last name cannot be empty")
  @Size(max = 50, message = "Last name cannot exceed 50 characters")
  private String lastName;

  @Email(message = "Email address should be valid")
  @NotEmpty(message = "Email address cannot be empty")
  @Indexed(unique = true) // Ensures email addresses are unique in the database
  private String emailAddress;

  @NotEmpty(message = "Password cannot be empty")
  @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
  private String password;

  private List<Integer> portfolioList;
}
