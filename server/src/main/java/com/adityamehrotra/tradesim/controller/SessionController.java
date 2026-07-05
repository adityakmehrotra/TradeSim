package com.adityamehrotra.tradesim.controller;

import com.adityamehrotra.tradesim.model.Session;
import com.adityamehrotra.tradesim.service.SessionService;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
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
  private static final String COOKIE_NAME = "tradesim_session";

  private final SessionService sessionService;

  public SessionController(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @GetMapping
  public ResponseEntity<?> currentSession(
      @CookieValue(name = COOKIE_NAME, required = false) String token,
      HttpServletResponse response) {
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
