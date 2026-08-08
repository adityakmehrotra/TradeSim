package com.adityamehrotra.tradesim.web;

import com.adityamehrotra.tradesim.exception.SessionRequiredException;
import com.adityamehrotra.tradesim.model.Session;
import com.adityamehrotra.tradesim.service.SessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Hands controllers the caller's session, or refuses the request. Resolving in one place is what
 * keeps session creation to the single endpoint that hands the cookie back: anywhere else, creating
 * a session would leave the caller without the cookie that identifies it, so the row would be
 * orphaned the moment it was written.
 */
@Component
public class SessionArgumentResolver implements HandlerMethodArgumentResolver {
  private final SessionService sessionService;

  public SessionArgumentResolver(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return Session.class.equals(parameter.getParameterType());
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
    Session session = sessionService.find(token(request));
    if (session == null) {
      throw new SessionRequiredException();
    }
    return session;
  }

  private String token(HttpServletRequest request) {
    if (request == null || request.getCookies() == null) {
      return null;
    }
    for (Cookie cookie : request.getCookies()) {
      if (SessionService.COOKIE_NAME.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
