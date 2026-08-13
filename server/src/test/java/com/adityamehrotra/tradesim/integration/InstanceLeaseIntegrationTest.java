package com.adityamehrotra.tradesim.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.adityamehrotra.tradesim.startup.InstanceLease;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

/** Two instances against one database, which is the case the in-memory books cannot survive. */
class InstanceLeaseIntegrationTest extends IntegrationTestBase {
  private static final long HELD_FOR_A_MINUTE = 60_000;
  private static final long ALREADY_EXPIRED = 0;

  @Autowired private MongoTemplate mongoTemplate;

  private InstanceLease lease(long holdMillis) {
    return new InstanceLease(mongoTemplate, true, holdMillis);
  }

  @Test
  void aSecondInstanceRefusesToStartWhileTheLeaseIsHeld() {
    lease(HELD_FOR_A_MINUTE).acquire();
    InstanceLease second = lease(HELD_FOR_A_MINUTE);

    assertThrows(IllegalStateException.class, second::acquire);
    assertFalse(second.isHeld());
  }

  @Test
  void takesOverALeaseWhoseOwnerIsGone() {
    lease(ALREADY_EXPIRED).acquire();

    InstanceLease replacement = lease(HELD_FOR_A_MINUTE);
    replacement.acquire();

    assertTrue(replacement.isHeld());
  }

  @Test
  void noticesWhenTheLeaseIsTakenAway() {
    InstanceLease first = lease(ALREADY_EXPIRED);
    first.acquire();
    lease(HELD_FOR_A_MINUTE).acquire();

    first.renew();

    assertFalse(first.isHeld());
  }
}
