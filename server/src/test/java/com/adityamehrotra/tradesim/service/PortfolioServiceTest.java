package com.adityamehrotra.tradesim.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.adityamehrotra.tradesim.dto.PortfolioRequest;
import com.adityamehrotra.tradesim.exception.PortfolioNotFoundException;
import com.adityamehrotra.tradesim.market.MarketService;
import com.adityamehrotra.tradesim.model.Portfolio;
import com.adityamehrotra.tradesim.repository.PortfolioRepository;
import com.adityamehrotra.tradesim.repository.PositionRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** The ownership guard sits in the service, so nothing mutates a portfolio the caller lacks. */
@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {
  private static final int OWNER = 1;
  private static final int OTHER = 2;
  private static final int PORTFOLIO_ID = 7;

  @Mock private PortfolioRepository portfolioRepository;
  @Mock private PositionRepository positionRepository;
  @Mock private MarketService marketService;
  @Mock private SequenceService sequenceService;

  private PortfolioService service() {
    return new PortfolioService(
        portfolioRepository, positionRepository, marketService, sequenceService);
  }

  @Test
  void refusesToRenameAPortfolioTheAccountDoesNotOwn() {
    when(portfolioRepository.findByPortfolioIDAndAccountID(PORTFOLIO_ID, OTHER)).thenReturn(null);

    assertThrows(
        PortfolioNotFoundException.class,
        () -> service().updatePortfolioName(PORTFOLIO_ID, OTHER, "Theirs"));
    verify(portfolioRepository, never()).save(any());
  }

  @Test
  void refusesToDeleteAPortfolioTheAccountDoesNotOwn() {
    when(portfolioRepository.findByPortfolioIDAndAccountID(PORTFOLIO_ID, OTHER)).thenReturn(null);

    assertThrows(
        PortfolioNotFoundException.class, () -> service().deletePortfolio(PORTFOLIO_ID, OTHER));
    verify(marketService, never()).cancelAllForPortfolio(anyInt());
    verify(positionRepository, never()).deleteAll(any());
    verify(portfolioRepository, never()).delete(any());
  }

  /**
   * Reservations have to be released while the portfolio is still there to release them against.
   */
  @Test
  void cancelsOrdersAndClearsPositionsBeforeDeletingThePortfolio() {
    Portfolio portfolio = new Portfolio();
    portfolio.setPortfolioID(PORTFOLIO_ID);
    when(portfolioRepository.findByPortfolioIDAndAccountID(PORTFOLIO_ID, OWNER))
        .thenReturn(portfolio);
    when(positionRepository.findByPortfolioID(PORTFOLIO_ID)).thenReturn(List.of());

    service().deletePortfolio(PORTFOLIO_ID, OWNER);

    InOrder order = inOrder(marketService, positionRepository, portfolioRepository);
    order.verify(marketService).cancelAllForPortfolio(PORTFOLIO_ID);
    order.verify(positionRepository).deleteAll(any());
    order.verify(portfolioRepository).delete(portfolio);
  }

  @Test
  void treatsAMissingPortfolioTheSameAsAForeignOne() {
    when(portfolioRepository.findByPortfolioIDAndAccountID(PORTFOLIO_ID, OWNER)).thenReturn(null);

    PortfolioNotFoundException missing =
        assertThrows(
            PortfolioNotFoundException.class, () -> service().requireOwned(PORTFOLIO_ID, OWNER));
    PortfolioNotFoundException foreign =
        assertThrows(
            PortfolioNotFoundException.class, () -> service().requireOwned(PORTFOLIO_ID, OTHER));

    assertEquals(missing.getMessage(), foreign.getMessage());
  }

  @Test
  void refusesMoreThanTheAllowedNumberOfPortfolios() {
    when(portfolioRepository.countByAccountID(OWNER)).thenReturn(10L);

    IllegalArgumentException e =
        assertThrows(
            IllegalArgumentException.class,
            () -> service().createPortfolio(request(1000.0, "Name", "Description")));
    assertEquals("An account can hold at most 10 portfolios", e.getMessage());
    verify(portfolioRepository, never()).save(any());
  }

  @Test
  void refusesAnInitialBalanceOutsideTheAllowedRange() {
    assertThrows(
        IllegalArgumentException.class,
        () -> service().createPortfolio(request(1.0, "Name", "Description")));
    assertThrows(
        IllegalArgumentException.class,
        () -> service().createPortfolio(request(50_000_000.0, "Name", "Description")));
    verify(portfolioRepository, never()).save(any());
  }

  @Test
  void refusesNamesAndDescriptionsThatAreTooLong() {
    assertThrows(
        IllegalArgumentException.class,
        () -> service().createPortfolio(request(1000.0, "n".repeat(61), "Description")));
    assertThrows(
        IllegalArgumentException.class,
        () -> service().createPortfolio(request(1000.0, "Name", "d".repeat(201))));
    verify(portfolioRepository, never()).save(any());
  }

  private PortfolioRequest request(double initialBalance, String name, String description) {
    return new PortfolioRequest(OWNER, name, description, initialBalance, initialBalance);
  }
}
