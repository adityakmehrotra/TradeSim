package com.adityamehrotra.tradesim.market;

/**
 * A tradable symbol in the simulated market. The reference price seeds the book and anchors the
 * random walk; drift and volatility are per-tick basis points that steer the market maker's quotes.
 */
public record Instrument(
    String symbol, String name, long referencePriceCents, int driftBps, int volatilityBps) {}
