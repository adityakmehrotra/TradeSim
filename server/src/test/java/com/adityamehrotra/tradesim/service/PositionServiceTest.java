package com.adityamehrotra.tradesim.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.adityamehrotra.tradesim.model.Lot;
import com.adityamehrotra.tradesim.model.Portfolio;
import com.adityamehrotra.tradesim.model.Position;
import com.adityamehrotra.tradesim.repository.PortfolioRepository;
import com.adityamehrotra.tradesim.repository.PositionRepository;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PositionServiceTest {
  @Mock private PortfolioRepository portfolioRepository;
  @Mock private PositionRepository positionRepository;
  @InjectMocks private PositionService service;

  private Portfolio portfolio(long cashCents, long reservedCents) {
    Portfolio portfolio = new Portfolio();
    portfolio.setCashCents(cashCents);
    portfolio.setReservedCashCents(reservedCents);
    return portfolio;
  }

  @Test
  void reservesCashWhenBuyingPowerCovers() {
    Portfolio portfolio = portfolio(100_000, 0);
    when(portfolioRepository.findByPortfolioID(1)).thenReturn(portfolio);

    assertTrue(service.reserveCash(1, 50000));
    assertEquals(50_000, portfolio.getReservedCashCents());
  }

  @Test
  void refusesToReserveMoreCashThanAvailable() {
    Portfolio portfolio = portfolio(10_000, 0);
    when(portfolioRepository.findByPortfolioID(1)).thenReturn(portfolio);

    assertFalse(service.reserveCash(1, 50000));
    verify(portfolioRepository, never()).save(any());
  }

  @Test
  void buyFillSpendsCashReleasesReserveAndAddsLot() {
    Portfolio portfolio = portfolio(100_000, 50_000);
    when(portfolioRepository.findByPortfolioID(1)).thenReturn(portfolio);
    when(positionRepository.findByPortfolioIDAndSymbol(1, "NOVA")).thenReturn(null);

    service.applyBuyFill(1, "NOVA", 10, 5000, 5000);

    assertEquals(50_000, portfolio.getCashCents());
    assertEquals(0, portfolio.getReservedCashCents());

    ArgumentCaptor<Position> saved = ArgumentCaptor.forClass(Position.class);
    verify(positionRepository).save(saved.capture());
    assertEquals(10, saved.getValue().getQuantity());
    assertEquals(1, saved.getValue().getLots().size());
  }

  @Test
  void sellFillConsumesLotsFifoAndRealizesProfit() {
    Position position = new Position(1, "NOVA");
    position.setQuantity(20);
    position.setReservedQuantity(15);
    position.getLots().add(new Lot(10, 1000));
    position.getLots().add(new Lot(10, 2000));
    Portfolio portfolio = portfolio(0, 0);
    when(portfolioRepository.findByPortfolioID(1)).thenReturn(portfolio);
    when(positionRepository.findByPortfolioIDAndSymbol(1, "NOVA")).thenReturn(position);

    service.applySellFill(1, "NOVA", 15, 2500);

    assertEquals(37_500, portfolio.getCashCents());
    assertEquals(5, position.getQuantity());
    assertEquals(0, position.getReservedQuantity());
    assertEquals(17_500, position.getRealizedPnlCents());
    assertEquals(1, position.getLots().size());
    assertEquals(5, position.getLots().get(0).getQuantity());
  }

  /**
   * The reason money moved to integers. Prices like 3.33 cannot be held exactly as dollars, so a
   * long run of fills used to leave the balance off by fractions of a cent. Every amount here is
   * awkward on purpose, and the balance still has to match to the cent.
   */
  @Test
  void aLongRunOfFillsDoesNotDrift() {
    Portfolio portfolio = portfolio(100_000_000, 0);
    Position position = new Position(1, "NOVA");
    when(portfolioRepository.findByPortfolioID(1)).thenReturn(portfolio);
    when(positionRepository.findByPortfolioIDAndSymbol(1, "NOVA")).thenReturn(position);

    Random random = new Random(11);
    long expectedCents = 100_000_000;
    long held = 0;

    for (int i = 0; i < 500; i++) {
      long priceCents = 1 + random.nextInt(9999);
      long quantity = 1 + random.nextInt(20);
      if (held < quantity || random.nextBoolean()) {
        service.applyBuyFill(1, "NOVA", quantity, priceCents, 0);
        expectedCents -= priceCents * quantity;
        held += quantity;
      } else {
        service.applySellFill(1, "NOVA", quantity, priceCents);
        expectedCents += priceCents * quantity;
        held -= quantity;
      }
    }

    assertEquals(expectedCents, portfolio.getCashCents());
    assertEquals(held, position.getQuantity());
  }

  /** The dollar field is what a rolled back version would read, so it cannot fall behind. */
  @Test
  void everyChangeKeepsTheDollarFieldInStep() {
    Portfolio portfolio = portfolio(100_000, 0);
    when(portfolioRepository.findByPortfolioID(1)).thenReturn(portfolio);
    when(positionRepository.findByPortfolioIDAndSymbol(1, "NOVA")).thenReturn(null);

    service.applyBuyFill(1, "NOVA", 3, 3_333, 0);

    assertEquals(90_001, portfolio.getCashCents());
    assertEquals(900.01, portfolio.getCash());
  }

  @Test
  void releasingAReservationReturnsExactlyWhatWasHeld() {
    Portfolio portfolio = portfolio(100_000, 0);
    when(portfolioRepository.findByPortfolioID(1)).thenReturn(portfolio);

    assertTrue(service.reserveCash(1, 33_333));
    service.releaseCash(1, 33_333);

    assertEquals(0, portfolio.getReservedCashCents());
    assertEquals(100_000, portfolio.availableCashCents());
  }

  @Test
  void refusesToReserveMoreSharesThanAreFree() {
    Position position = new Position(1, "NOVA");
    position.setQuantity(10);
    position.setReservedQuantity(8);
    when(positionRepository.findByPortfolioIDAndSymbol(1, "NOVA")).thenReturn(position);

    assertFalse(service.reserveShares(1, "NOVA", 5));
    verify(positionRepository, never()).save(any());
  }
}
