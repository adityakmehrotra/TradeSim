package com.adityamehrotra.tradesim.startup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.adityamehrotra.tradesim.service.CentsMigration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;
import org.springframework.dao.DataAccessResourceFailureException;

/** Readiness has to follow the sweep, not the fact that the process came up. */
class StartupHealthIndicatorTest {
  private static final long RETRY_MS = 200;

  @Test
  void staysDownUntilTheSweepSucceeds() throws Exception {
    ReservationSweep sweep = mock(ReservationSweep.class);
    doThrow(new DataAccessResourceFailureException("mongo is away")).doNothing().when(sweep).run();
    StartupReconciliation reconciliation =
        new StartupReconciliation(mock(CentsMigration.class), sweep, RETRY_MS, RETRY_MS);
    StartupHealthIndicator indicator = new StartupHealthIndicator(reconciliation);

    assertEquals(Status.DOWN, indicator.health().getStatus());

    reconciliation.reconcile();
    assertEquals(Status.DOWN, indicator.health().getStatus());

    for (int waited = 0; waited < 5000 && !reconciliation.isComplete(); waited += 20) {
      Thread.sleep(20);
    }
    assertEquals(Status.UP, indicator.health().getStatus());
  }
}
