package com.adityamehrotra.paper_trader.model;

import java.util.List;
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
@Document(collection = "Paper Trader- Account")
public class Account {
  @Id private Integer accountID;
  private String firstName;
  private String lastName;
  private String emailAddress;
  private String password;
  private List<Integer> portfolioList;
}
