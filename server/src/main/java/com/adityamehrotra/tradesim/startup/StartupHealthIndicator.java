package com.adityamehrotra.tradesim.startup;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Holds readiness down until reconciliation has run, so a proxy or an orchestrator does not send
 * trading traffic to an instance whose reservations are still wrong.
 */
@Component
public class StartupHealthIndicator implements HealthIndicator {
  private final StartupReconciliation reconciliation;

  public StartupHealthIndicator(StartupReconciliation reconciliation) {
    this.reconciliation = reconciliation;
  }

  @Override
  public Health health() {
    return reconciliation.isComplete() ? Health.up().build() : Health.down().build();
  }
}
