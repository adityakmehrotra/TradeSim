package com.adityamehrotra.tradesim.startup;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/** Drops readiness if this instance ever stops being the one that owns the market. */
@Component
public class InstanceLeaseHealthIndicator implements HealthIndicator {
  private final InstanceLease lease;

  public InstanceLeaseHealthIndicator(InstanceLease lease) {
    this.lease = lease;
  }

  @Override
  public Health health() {
    return lease.isHeld() ? Health.up().build() : Health.down().build();
  }
}
