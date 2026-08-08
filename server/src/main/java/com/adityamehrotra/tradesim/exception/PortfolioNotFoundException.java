package com.adityamehrotra.tradesim.exception;

/**
 * Raised when a portfolio does not exist, or exists but belongs to another session. Both cases
 * share this one exception on purpose: telling them apart would let a caller map out which ids are
 * real by walking small numbers.
 */
public class PortfolioNotFoundException extends RuntimeException {
  public PortfolioNotFoundException() {
    super("No such portfolio");
  }
}
