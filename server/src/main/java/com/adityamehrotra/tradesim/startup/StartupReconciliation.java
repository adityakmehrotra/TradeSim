package com.adityamehrotra.tradesim.startup;

import com.adityamehrotra.tradesim.service.CentsMigration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Runs the database work this instance owes before it can accept orders: the required migration
 * first, then the sweep that releases reservations held over from the previous run.
 *
 * <p>The first attempt runs while beans are still being created, so a healthy start is finished
 * before the HTTP listener opens. A database that is briefly unreachable used to be logged once and
 * skipped, which left every stale reservation in place and the account short of buying power with
 * nothing on screen to explain it. Now the attempt repeats on a background thread with a growing
 * delay, readiness stays down, and order placement is refused until it succeeds.
 */
@Component
public class StartupReconciliation {
  private static final Logger log = LoggerFactory.getLogger(StartupReconciliation.class);

  private final CentsMigration migration;
  private final ReservationSweep sweep;
  private final long firstDelayMillis;
  private final long maxDelayMillis;

  private final ScheduledExecutorService retries =
      Executors.newSingleThreadScheduledExecutor(
          task -> {
            Thread thread = new Thread(task, "startup-reconciliation");
            thread.setDaemon(true);
            return thread;
          });

  private volatile boolean complete;
  private int attempts;

  public StartupReconciliation(
      CentsMigration migration,
      ReservationSweep sweep,
      @Value("${tradesim.startup.first-retry-ms:1000}") long firstDelayMillis,
      @Value("${tradesim.startup.max-retry-ms:30000}") long maxDelayMillis) {
    this.migration = migration;
    this.sweep = sweep;
    this.firstDelayMillis = firstDelayMillis;
    this.maxDelayMillis = maxDelayMillis;
  }

  @PostConstruct
  void reconcile() {
    if (!attempt()) {
      retryIn(firstDelayMillis);
    }
  }

  public boolean isComplete() {
    return complete;
  }

  /** Refuses work that would read or write a reservation the sweep has not corrected yet. */
  public void requireComplete() {
    if (!complete) {
      throw new StartupIncompleteException("The service is still reconciling its startup state");
    }
  }

  private void retryIn(long delayMillis) {
    retries.schedule(
        () -> {
          if (!attempt()) {
            retryIn(Math.min(delayMillis * 2, maxDelayMillis));
          }
        },
        delayMillis,
        TimeUnit.MILLISECONDS);
  }

  /**
   * Anything thrown here means the instance cannot be trusted with an order, so every runtime
   * failure is treated the same way rather than only the ones the driver reports.
   */
  private boolean attempt() {
    attempts++;
    try {
      migration.migrate();
      sweep.run();
      complete = true;
      if (attempts > 1) {
        log.info("Startup reconciliation finished on attempt {}", attempts);
      }
      return true;
    } catch (RuntimeException e) {
      log.warn("Startup reconciliation attempt {} failed: {}", attempts, e.toString());
      return false;
    }
  }

  @PreDestroy
  void stopRetrying() {
    retries.shutdownNow();
  }
}
