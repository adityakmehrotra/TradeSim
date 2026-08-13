package com.adityamehrotra.tradesim.exception;

/** Raised when a caller has used up a fixed window's allowance. */
public class RateLimitedException extends RuntimeException {
  public RateLimitedException(String message) {
    super(message);
  }
}
