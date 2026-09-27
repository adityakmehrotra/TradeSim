package com.adityamehrotra.tradesim.market;

/**
 * A tradable symbol in the simulated market. The reference price seeds the book and anchors the
 * random walk. Drift is basis points per day and volatility is basis points per one second tick;
 * both steer the market maker's quotes.
 */
public record Instrument(
    String symbol, String name, long referencePriceCents, int dailyDriftBps, int volatilityBps) {

  private static final double SECONDS_PER_DAY = 86_400;

  /** The market ticks once a second, so a day's drift is spread across the seconds in a day. */
  double driftBpsPerSecond() {
    return dailyDriftBps / SECONDS_PER_DAY;
  }
}
