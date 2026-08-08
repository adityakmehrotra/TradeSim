package com.adityamehrotra.tradesim.web;

import com.adityamehrotra.tradesim.exception.PortfolioNotFoundException;
import com.adityamehrotra.tradesim.exception.SessionRequiredException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Turns the exceptions the API throws into one error shape, so the client parses one thing. */
@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(SessionRequiredException.class)
  public ResponseEntity<Map<String, String>> sessionRequired(SessionRequiredException e) {
    return body(HttpStatus.UNAUTHORIZED, e.getMessage());
  }

  @ExceptionHandler(PortfolioNotFoundException.class)
  public ResponseEntity<Map<String, String>> portfolioNotFound(PortfolioNotFoundException e) {
    return body(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
    return body(HttpStatus.BAD_REQUEST, message(e));
  }

  private static ResponseEntity<Map<String, String>> body(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(Map.of("error", message));
  }

  private static String message(Exception e) {
    return e.getMessage() == null ? "Invalid request" : e.getMessage();
  }
}
