package com.adityamehrotra.tradesim.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adityamehrotra.tradesim.market.MarketService;
import com.adityamehrotra.tradesim.model.Session;
import com.adityamehrotra.tradesim.service.PositionService;
import com.adityamehrotra.tradesim.service.SessionService;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Covers the ownership checks that keep one session out of another session's portfolio. */
@WebMvcTest(OrderController.class)
class OrderControllerTest {
  private static final int ACCOUNT_ID = 1;
  private static final int FOREIGN_PORTFOLIO = 7;
  private static final String ORDER_BODY =
      "{\"portfolioID\":7,\"symbol\":\"NVDA\",\"side\":\"BUY\",\"type\":\"MARKET\",\"quantity\":5}";

  @Autowired private MockMvc mockMvc;

  @MockBean private MarketService marketService;
  @MockBean private PositionService positionService;
  @MockBean private SessionService sessionService;

  @BeforeEach
  void resolveSession() {
    when(sessionService.getOrCreate(any())).thenReturn(new Session("token", ACCOUNT_ID));
  }

  private Cookie cookie() {
    return new Cookie("tradesim_session", "token");
  }

  @Test
  void refusesToPlaceAnOrderAgainstAnotherSessionsPortfolio() throws Exception {
    when(positionService.ownedBy(FOREIGN_PORTFOLIO, ACCOUNT_ID)).thenReturn(false);

    mockMvc
        .perform(
            post("/tradesim/api/order")
                .cookie(cookie())
                .contentType(MediaType.APPLICATION_JSON)
                .content(ORDER_BODY))
        .andExpect(status().isForbidden());

    verify(marketService, never())
        .placeOrder(anyInt(), anyInt(), anyString(), any(), any(), any(), anyLong());
  }

  @Test
  void placesAnOrderAgainstAnOwnedPortfolio() throws Exception {
    when(positionService.ownedBy(FOREIGN_PORTFOLIO, ACCOUNT_ID)).thenReturn(true);
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
  void refusesToReadAnotherSessionsPositions() throws Exception {
    when(positionService.ownedBy(FOREIGN_PORTFOLIO, ACCOUNT_ID)).thenReturn(false);

    mockMvc
        .perform(get("/tradesim/api/order/positions").cookie(cookie()).param("portfolioID", "7"))
        .andExpect(status().isForbidden());

    verify(positionService, never()).positionsFor(anyInt());
  }

  @Test
  void readsPositionsForAnOwnedPortfolio() throws Exception {
    when(positionService.ownedBy(FOREIGN_PORTFOLIO, ACCOUNT_ID)).thenReturn(true);
    when(positionService.positionsFor(FOREIGN_PORTFOLIO)).thenReturn(List.of());

    mockMvc
        .perform(get("/tradesim/api/order/positions").cookie(cookie()).param("portfolioID", "7"))
        .andExpect(status().isOk());

    verify(positionService).positionsFor(FOREIGN_PORTFOLIO);
  }
}
