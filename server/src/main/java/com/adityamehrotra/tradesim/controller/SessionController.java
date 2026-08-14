package com.adityamehrotra.tradesim.controller;

import com.adityamehrotra.tradesim.exception.RateLimitedException;
import com.adityamehrotra.tradesim.model.Session;
import com.adityamehrotra.tradesim.service.SessionService;
import com.adityamehrotra.tradesim.web.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tradesim/api/session")
public class SessionController {
  private static final String COOKIE_NAME = SessionService.COOKIE_NAME;

  private final SessionService sessionService;
  private final RateLimiter rateLimiter;
  private final int newSessionsPerHour;

  public SessionController(
      SessionService sessionService,
      RateLimiter rateLimiter,
      @Value("${tradesim.limits.new-sessions-per-hour:30}") int newSessionsPerHour) {
    this.sessionService = sessionService;
    this.rateLimiter = rateLimiter;
    this.newSessionsPerHour = newSessionsPerHour;
  }

  @GetMapping
  public ResponseEntity<?> currentSession(
      @CookieValue(name = COOKIE_NAME, required = false) String token,
      HttpServletRequest request,
      HttpServletResponse response) {
    // Only a caller without a usable session costs anything, so returning one is never limited.
    if (sessionService.find(token) == null) {
      requireNewSessionAllowance(request);
    }
    Session session = sessionService.getOrCreate(token);
    attachCookieIfNew(token, session, response);
    return ResponseEntity.ok(sessionBody(session));
  }

  @PostMapping("/reset")
  public ResponseEntity<?> reset(
      @CookieValue(name = COOKIE_NAME, required = false) String token,
      HttpServletResponse response) {
    Session session = sessionService.getOrCreate(token);
    attachCookieIfNew(token, session, response);
    sessionService.reset(session);
    return ResponseEntity.ok(sessionBody(session));
  }

  /**
   * Keyed on the address the proxy reports. Reading a forwarded header directly would let a caller
   * pick its own key and walk straight past this.
   */
  private void requireNewSessionAllowance(HttpServletRequest request) {
    if (!rateLimiter.allow("session:" + request.getRemoteAddr(), newSessionsPerHour, 3_600_000L)) {
      throw new RateLimitedException("Too many new sessions from this address. Try again later.");
    }
  }

  private void attachCookieIfNew(String token, Session session, HttpServletResponse response) {
    if (session.getSessionId().equals(token)) {
      return;
    }
    ResponseCookie cookie =
        ResponseCookie.from(COOKIE_NAME, session.getSessionId())
            .httpOnly(true)
            .path("/")
            .sameSite("Lax")
            .maxAge(Duration.ofDays(30))
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  private Map<String, Object> sessionBody(Session session) {
    Map<String, Object> body = new HashMap<>();
    body.put("accountId", session.getAccountID());
    body.put("portfolios", sessionService.portfoliosFor(session));
    return body;
  }
}
