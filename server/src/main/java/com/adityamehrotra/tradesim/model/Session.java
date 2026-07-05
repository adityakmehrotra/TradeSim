package com.adityamehrotra.tradesim.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * An anonymous browser session. The sessionId is the value stored in the client cookie. Each
 * session owns one accountID, which its portfolios are keyed on, so there are no user accounts or
 * passwords.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "TradeSim-Session")
public class Session {
  @Id private String sessionId;

  private Integer accountID;
}
