package com.adityamehrotra.tradesim.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adityamehrotra.tradesim.dto.PortfolioRequest;
import com.adityamehrotra.tradesim.exception.PortfolioNotFoundException;
import com.adityamehrotra.tradesim.model.Portfolio;
import com.adityamehrotra.tradesim.model.Session;
import com.adityamehrotra.tradesim.service.PortfolioService;
import com.adityamehrotra.tradesim.service.SessionService;
import com.adityamehrotra.tradesim.web.SessionArgumentResolver;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** Every portfolio route needs a session, and only ever reaches that session's own portfolios. */
@WebMvcTest(PortfolioController.class)
@Import(SessionArgumentResolver.class)
class PortfolioControllerTest {
  private static final int ACCOUNT_ID = 1;
  private static final int FOREIGN = 7;
  private static final int MISSING = 999;

  @Autowired private MockMvc mockMvc;

  @MockBean private PortfolioService portfolioService;
  @MockBean private SessionService sessionService;

  @BeforeEach
  void resolveSession() {
    when(sessionService.find("token")).thenReturn(new Session("token", ACCOUNT_ID));
  }

  private Cookie cookie() {
    return new Cookie(SessionService.COOKIE_NAME, "token");
  }

  @Test
  void readsAnOwnedPortfolio() throws Exception {
    when(portfolioService.requireOwned(FOREIGN, ACCOUNT_ID)).thenReturn(new Portfolio());

    mockMvc
        .perform(get("/tradesim/api/portfolio/get").cookie(cookie()).param("portfolioID", "7"))
        .andExpect(status().isOk());
  }

  /** A portfolio someone else owns has to look exactly like one that was never there. */
  @Test
  void reportsForeignAndMissingPortfoliosIdentically() throws Exception {
    when(portfolioService.requireOwned(FOREIGN, ACCOUNT_ID))
        .thenThrow(new PortfolioNotFoundException());
    when(portfolioService.requireOwned(MISSING, ACCOUNT_ID))
        .thenThrow(new PortfolioNotFoundException());

    MvcResult foreign =
        mockMvc
            .perform(get("/tradesim/api/portfolio/get").cookie(cookie()).param("portfolioID", "7"))
            .andExpect(status().isNotFound())
            .andReturn();
    MvcResult missing =
        mockMvc
            .perform(
                get("/tradesim/api/portfolio/get").cookie(cookie()).param("portfolioID", "999"))
            .andExpect(status().isNotFound())
            .andReturn();

    assertEquals(
        foreign.getResponse().getContentAsString(), missing.getResponse().getContentAsString());
  }

  @Test
  void refusesToRenameAForeignPortfolio() throws Exception {
    doThrowOnRename();

    mockMvc
        .perform(
            put("/tradesim/api/portfolio/name")
                .cookie(cookie())
                .param("portfolioID", "7")
                .param("name", "Theirs"))
        .andExpect(status().isNotFound());
  }

  @Test
  void refusesToChangeAForeignPortfolioDescription() throws Exception {
    org.mockito.Mockito.doThrow(new PortfolioNotFoundException())
        .when(portfolioService)
        .updatePortfolioDescription(FOREIGN, ACCOUNT_ID, "Theirs");

    mockMvc
        .perform(
            put("/tradesim/api/portfolio/description")
                .cookie(cookie())
                .param("portfolioID", "7")
                .param("description", "Theirs"))
        .andExpect(status().isNotFound());
  }

  @Test
  void refusesToDeleteAForeignPortfolio() throws Exception {
    org.mockito.Mockito.doThrow(new PortfolioNotFoundException())
        .when(portfolioService)
        .deletePortfolio(FOREIGN, ACCOUNT_ID);

    mockMvc
        .perform(
            delete("/tradesim/api/portfolio/delete").cookie(cookie()).param("portfolioID", "7"))
        .andExpect(status().isNotFound());
  }

  /** The body can claim any account it likes. The session decides who owns what gets created. */
  @Test
  void createsPortfoliosForTheSessionAccountOnly() throws Exception {
    when(portfolioService.createPortfolio(org.mockito.ArgumentMatchers.any())).thenReturn(3);

    mockMvc
        .perform(
            post("/tradesim/api/portfolio/create")
                .cookie(cookie())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"accountID\":999,\"name\":\"Mine\",\"description\":\"d\",\"cash\":1000.0,"
                        + "\"initialBalance\":1000.0}"))
        .andExpect(status().isCreated());

    ArgumentCaptor<PortfolioRequest> saved = ArgumentCaptor.forClass(PortfolioRequest.class);
    verify(portfolioService).createPortfolio(saved.capture());
    assertEquals(ACCOUNT_ID, saved.getValue().getAccountID());
  }

  @Test
  void refusesRequestsThatCarryNoSession() throws Exception {
    mockMvc
        .perform(get("/tradesim/api/portfolio/get").param("portfolioID", "7"))
        .andExpect(status().isUnauthorized());

    verify(portfolioService, never()).requireOwned(anyInt(), anyInt());
  }

  private void doThrowOnRename() {
    org.mockito.Mockito.doThrow(new PortfolioNotFoundException())
        .when(portfolioService)
        .updatePortfolioName(anyInt(), anyInt(), anyString());
  }
}
