package com.adityamehrotra.tradesim.web;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Fixed window counting, kept deliberately small. The demo is open to anyone, so the point is to
 * stop one caller from opening thousands of sessions or hammering the order endpoint, not to police
 * traffic precisely.
 *
 * <p>The map is capped. A counter keyed by caller is itself something an attacker can grow, so once
 * it is full the expired entries are dropped, and if that frees nothing the request is allowed
 * rather than refused: failing open keeps a full map from locking everyone out.
 */
@Component
public class RateLimiter {
  private final Map<String, Window> windows = new ConcurrentHashMap<>();
  private final int maxKeys;
  private final LongSupplier clock;

  @Autowired
  public RateLimiter(@Value("${tradesim.limits.max-tracked-keys:50000}") int maxKeys) {
    this(maxKeys, System::currentTimeMillis);
  }

  /** Lets a test move time by hand instead of sleeping and hoping the clock ticked. */
  RateLimiter(int maxKeys, LongSupplier clock) {
    this.maxKeys = maxKeys;
    this.clock = clock;
  }

  /** True when the caller may proceed. */
  public boolean allow(String key, int limit, long windowMillis) {
    long now = clock.getAsLong();

    if (windows.size() >= maxKeys) {
      evictExpired(now);
      if (windows.size() >= maxKeys) {
        return true;
      }
    }

    Window window =
        windows.compute(
            key,
            (ignored, existing) -> {
              if (existing == null || now - existing.startedAt >= windowMillis) {
                return new Window(now);
              }
              return existing;
            });
    return window.count.incrementAndGet() <= limit;
  }

  private void evictExpired(long now) {
    // Anything older than a day is expired under every window this application uses.
    windows.entrySet().removeIf(entry -> now - entry.getValue().startedAt > 86_400_000L);
  }

  int trackedKeys() {
    return windows.size();
  }

  private static final class Window {
    private final long startedAt;
    private final AtomicInteger count = new AtomicInteger();

    private Window(long startedAt) {
      this.startedAt = startedAt;
    }
  }
}
