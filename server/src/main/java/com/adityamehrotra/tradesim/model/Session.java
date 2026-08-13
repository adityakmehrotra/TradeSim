package com.adityamehrotra.tradesim.model;

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
@NoArgsConstructor
@Document(collection = "TradeSim-Session")
public class Session {
  @Id private String sessionId;

  private Integer accountID;

  private Long createdAt;

  /**
   * When this session was last seen. Sessions are anonymous and nobody ever signs out, so this is
   * the only thing that separates one somebody still uses from one abandoned months ago.
   */
  private Long lastActive;

  public Session(String sessionId, Integer accountID) {
    this.sessionId = sessionId;
    this.accountID = accountID;
  }

  public Session(String sessionId, Integer accountID, long now) {
    this(sessionId, accountID);
    this.createdAt = now;
    this.lastActive = now;
  }

  /**
   * Sessions written before activity was tracked have no timestamps; treat them as last seen now.
   */
  public long lastActiveOr(long fallback) {
    return lastActive != null ? lastActive : fallback;
  }
}
