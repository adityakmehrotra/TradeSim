package com.adityamehrotra.tradesim.engine;

/** One aggregated price level in a depth snapshot. */
public record BookLevel(long priceCents, long quantity) {}
