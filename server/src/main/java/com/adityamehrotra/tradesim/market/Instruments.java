package com.adityamehrotra.tradesim.market;

import java.util.List;

/**
 * The symbols the simulated exchange lists. Prices are in cents. The tickers and names are real
 * companies, but every price here is synthetic; the seeds are just plausible starting points for
 * the random walk and track nothing in the real market.
 */
public final class Instruments {
  private Instruments() {}

  public static final List<Instrument> ALL =
      List.of(
          new Instrument("AAPL", "Apple Inc.", 23150, 0, 12),
          new Instrument("MSFT", "Microsoft Corp.", 42760, 1, 11),
          new Instrument("NVDA", "NVIDIA Corp.", 13890, 2, 24),
          new Instrument("AMZN", "Amazon.com Inc.", 20540, 1, 16),
          new Instrument("GOOGL", "Alphabet Inc.", 17820, 1, 14),
          new Instrument("TSLA", "Tesla Inc.", 24930, 0, 30),
          new Instrument("META", "Meta Platforms Inc.", 55710, 1, 18),
          new Instrument("JPM", "JPMorgan Chase & Co.", 24380, 0, 9));
}
