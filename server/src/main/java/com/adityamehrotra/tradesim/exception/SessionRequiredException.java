package com.adityamehrotra.tradesim.exception;

/** Raised when a request that needs an established session does not carry a usable cookie. */
public class SessionRequiredException extends RuntimeException {
  public SessionRequiredException() {
    super("This request needs a session. Load the app first so one can be created.");
  }
}
