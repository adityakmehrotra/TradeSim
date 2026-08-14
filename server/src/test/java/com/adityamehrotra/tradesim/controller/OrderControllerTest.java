package com.adityamehrotra.tradesim.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adityamehrotra.tradesim.exception.PortfolioNotFoundException;
import com.adityamehrotra.tradesim.market.MarketService;
import com.adityamehrotra.tradesim.model.Session;
import com.adityamehrotra.tradesim.service.PortfolioService;
import com.adityamehrotra.tradesim.service.PositionService;
import com.adityamehrotra.tradesim.service.SessionService;
import com.adityamehrotra.tradesim.startup.StartupIncompleteException;
import com.adityamehrotra.tradesim.startup.StartupReconciliation;
import com.adityamehrotra.tradesim.web.RateLimiter;
import com.adityamehrotra.tradesim.web.SessionArgumentResolver;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Covers the ownership checks that keep one session out of another session's portfolio. */
@WebMvcTest(OrderController.class)
@Import({SessionArgumentResolver.class, RateLimiter.class})
class OrderControllerTest {
  private static final int ACCOUNT_ID = 1;
  private static final int FOREIGN_PORTFOLIO = 7;
  private static final String ORDER_BODY =
      "{\"portfolioID\":7,\"symbol\":\"NVDA\",\"side\":\"BUY\",\"type\":\"MARKET\",\"quantity\":5}";

  @Autowired private MockMvc mockMvc;

  @MockBean private MarketService marketService;
  @MockBean private PositionService positionService;
  @MockBean private PortfolioService portfolioService;
  @MockBean private SessionService sessionService;
  @MockBean private StartupReconciliation reconciliation;

  @BeforeEach
  void resolveSession() {
    when(sessionService.find("token")).thenReturn(new Session("token", ACCOUNT_ID));
  }

  private Cookie cookie() {
    return new Cookie(SessionService.COOKIE_NAME, "token");
  }

  private void foreignPortfolio() {
    when(portfolioService.requireOwned(FOREIGN_PORTFOLIO, ACCOUNT_ID))
        .thenThrow(new PortfolioNotFoundException());
  }

  @Test
  void refusesToPlaceAnOrderAgainstAnotherSessionsPortfolio() throws Exception {
    foreignPortfolio();

    mockMvc
        .perform(
            post("/tradesim/api/order")
                .cookie(cookie())
                .contentType(MediaType.APPLICATION_JSON)
                .content(ORDER_BODY))
        .andExpect(status().isNotFound());

    verify(marketService, never())
        .placeOrder(anyInt(), anyInt(), anyString(), any(), any(), any(), anyLong());
  }

  @Test
  void placesAnOrderAgainstAnOwnedPortfolio() throws Exception {
    when(marketService.placeOrder(anyInt(), anyInt(), anyString(), any(), any(), any(), anyLong()))
        .thenReturn(new MarketService.PlaceResult(1, 5, 0, false));

    mockMvc
        .perform(
            post("/tradesim/api/order")
                .cookie(cookie())
                .contentType(MediaType.APPLICATION_JSON)
                .content(ORDER_BODY))
        .andExpect(status().isOk());

    verify(marketService)
        .placeOrder(anyInt(), anyInt(), anyString(), any(), any(), any(), anyLong());
  }

  @Test
  void refusesToPlaceAnOrderUntilStartupReconciliationFinishes() throws Exception {
    doThrow(new StartupIncompleteException("still reconciling"))
        .when(reconciliation)
        .requireComplete();

    mockMvc
        .perform(
            post("/tradesim/api/order")
                .cookie(cookie())
                .contentType(MediaType.APPLICATION_JSON)
                .content(ORDER_BODY))
        .andExpect(status().isServiceUnavailable());

    verify(portfolioService, never()).requireOwned(anyInt(), anyInt());
  }

  @Test
  void refusesToReadAnotherSessionsPositions() throws Exception {
    foreignPortfolio();

    mockMvc
        .perform(get("/tradesim/api/order/positions").cookie(cookie()).param("portfolioID", "7"))
        .andExpect(status().isNotFound());

    verify(positionService, never()).positionsFor(anyInt());
  }

  @Test
  void readsPositionsForAnOwnedPortfolio() throws Exception {
    when(positionService.positionsFor(FOREIGN_PORTFOLIO)).thenReturn(List.of());

    mockMvc
        .perform(get("/tradesim/api/order/positions").cookie(cookie()).param("portfolioID", "7"))
        .andExpect(status().isOk());

    verify(positionService).positionsFor(FOREIGN_PORTFOLIO);
  }

  @Test
  void refusesRequestsThatCarryNoSession() throws Exception {
    mockMvc
        .perform(get("/tradesim/api/order/positions").param("portfolioID", "7"))
        .andExpect(status().isUnauthorized());

    verify(portfolioService, never()).requireOwned(anyInt(), anyInt());
  }

  @Test
  void refusesRequestsWhoseCookieIsNotAKnownSession() throws Exception {
    mockMvc
        .perform(
            get("/tradesim/api/order/positions")
                .cookie(new Cookie(SessionService.COOKIE_NAME, "stale"))
                .param("portfolioID", "7"))
        .andExpect(status().isUnauthorized());

    verify(sessionService, never()).getOrCreate(eq("stale"));
  }
}
