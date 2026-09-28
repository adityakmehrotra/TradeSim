package com.adityamehrotra.tradesim.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/**
 * A counter keyed by caller is itself something to abuse, so the bound matters as much as the
 * count.
 */
class RateLimiterTest {

  @Test
  void allowsUpToTheLimitThenRefuses() {
    RateLimiter limiter = new RateLimiter(1000);

    for (int i = 0; i < 5; i++) {
      assertTrue(limiter.allow("caller", 5, 60_000), "call " + i + " should be allowed");
    }

    assertFalse(limiter.allow("caller", 5, 60_000));
  }

  @Test
  void countsEachCallerSeparately() {
    RateLimiter limiter = new RateLimiter(1000);

    assertTrue(limiter.allow("first", 1, 60_000));
    assertFalse(limiter.allow("first", 1, 60_000));
    assertTrue(limiter.allow("second", 1, 60_000), "one caller must not spend another's allowance");
  }

  @Test
  void startsAFreshWindowOnceTheOldOneHasPassed() {
    AtomicLong now = new AtomicLong(1_000);
    RateLimiter limiter = new RateLimiter(1000, now::get);

    assertTrue(limiter.allow("caller", 1, 60_000));
    assertFalse(limiter.allow("caller", 1, 60_000));

    now.addAndGet(59_999);
    assertFalse(limiter.allow("caller", 1, 60_000), "the window has not passed yet");

    now.incrementAndGet();
    assertTrue(limiter.allow("caller", 1, 60_000), "a new window should hand back the allowance");
  }

  /** Unbounded growth here would turn the limiter into the thing it is meant to prevent. */
  @Test
  void neverTracksMoreKeysThanItsCap() {
    RateLimiter limiter = new RateLimiter(50);

    for (int i = 0; i < 5000; i++) {
      limiter.allow("caller-" + i, 10, 60_000);
    }

    assertTrue(limiter.trackedKeys() <= 50, "tracked " + limiter.trackedKeys() + " keys");
  }

  /** A full map must not lock everyone out, so it fails open rather than closed. */
  @Test
  void keepsAllowingCallersOnceTheMapIsFull() {
    RateLimiter limiter = new RateLimiter(10);
    for (int i = 0; i < 500; i++) {
      limiter.allow("filler-" + i, 1, 60_000);
    }

    assertTrue(limiter.allow("someone-new", 1, 60_000));
  }

  @Test
  void doesNotHandOutMoreThanTheLimitUnderConcurrency() throws Exception {
    RateLimiter limiter = new RateLimiter(1000);
    int limit = 100;
    AtomicInteger allowed = new AtomicInteger();
    ExecutorService pool = Executors.newFixedThreadPool(8);

    try {
      java.util.List<Callable<Void>> work =
          IntStream.range(0, 8)
              .<Callable<Void>>mapToObj(
                  t ->
                      () -> {
                        for (int i = 0; i < 100; i++) {
                          if (limiter.allow("shared", limit, 60_000)) {
                            allowed.incrementAndGet();
                          }
                        }
                        return null;
                      })
              .toList();
      for (Future<Void> future : pool.invokeAll(work)) {
        future.get();
      }
    } finally {
      pool.shutdownNow();
    }

    assertEquals(limit, allowed.get());
  }
}
