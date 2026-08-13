package com.adityamehrotra.tradesim.startup;

/** Thrown by the paths that must not run before startup reconciliation has finished. */
public class StartupIncompleteException extends RuntimeException {
  public StartupIncompleteException(String message) {
    super(message);
  }
}
