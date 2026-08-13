package com.adityamehrotra.tradesim.startup;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.adityamehrotra.tradesim.service.CentsMigration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

/** A database that is away for a moment must not cost the account its reserved buying power. */
@ExtendWith(MockitoExtension.class)
class StartupReconciliationTest {
  private static final long RETRY_MS = 200;
  private static final long NEVER_MS = 60_000;

  @Mock private CentsMigration migration;
  @Mock private ReservationSweep sweep;

  private StartupReconciliation reconciliation(long retryMillis) {
    return new StartupReconciliation(migration, sweep, retryMillis, retryMillis);
  }

  private static void awaitComplete(StartupReconciliation reconciliation) throws Exception {
    for (int waited = 0; waited < 5000 && !reconciliation.isComplete(); waited += 20) {
      Thread.sleep(20);
    }
    assertTrue(reconciliation.isComplete());
  }

  @Test
  void migratesAndSweepsBeforeAnythingElseRuns() {
    StartupReconciliation reconciliation = reconciliation(RETRY_MS);

    reconciliation.reconcile();

    assertTrue(reconciliation.isComplete());
    verify(migration).migrate();
    verify(sweep).run();
  }

  @Test
  void retriesUntilTheSweepGetsThrough() throws Exception {
    doThrow(new DataAccessResourceFailureException("mongo is away")).doNothing().when(sweep).run();
    StartupReconciliation reconciliation = reconciliation(RETRY_MS);

    reconciliation.reconcile();
    awaitComplete(reconciliation);

    verify(sweep, times(2)).run();
    verify(migration, times(2)).migrate();
  }

  @Test
  void refusesOrdersWhileTheSweepIsStillFailing() {
    doThrow(new DataAccessResourceFailureException("mongo is away")).when(sweep).run();
    StartupReconciliation reconciliation = reconciliation(NEVER_MS);

    reconciliation.reconcile();

    assertFalse(reconciliation.isComplete());
    assertThrows(StartupIncompleteException.class, reconciliation::requireComplete);
    reconciliation.stopRetrying();
  }
}
