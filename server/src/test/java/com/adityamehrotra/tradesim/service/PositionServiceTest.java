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

  private Portfolio portfolio(double cash, double reserved) {
    Portfolio portfolio = new Portfolio();
    portfolio.setCash(cash);
    portfolio.setReservedCash(reserved);
    return portfolio;
  }

  @Test
  void reservesCashWhenBuyingPowerCovers() {
    Portfolio portfolio = portfolio(1000.0, 0.0);
    when(portfolioRepository.findByPortfolioID(1)).thenReturn(portfolio);

    assertTrue(service.reserveCash(1, 50000));
    assertEquals(500.0, portfolio.getReservedCash());
  }

  @Test
  void refusesToReserveMoreCashThanAvailable() {
    Portfolio portfolio = portfolio(100.0, 0.0);
    when(portfolioRepository.findByPortfolioID(1)).thenReturn(portfolio);

    assertFalse(service.reserveCash(1, 50000));
    verify(portfolioRepository, never()).save(any());
  }

  @Test
  void buyFillSpendsCashReleasesReserveAndAddsLot() {
    Portfolio portfolio = portfolio(1000.0, 500.0);
    when(portfolioRepository.findByPortfolioID(1)).thenReturn(portfolio);
    when(positionRepository.findByPortfolioIDAndSymbol(1, "NOVA")).thenReturn(null);

    service.applyBuyFill(1, "NOVA", 10, 5000, 5000);

    assertEquals(500.0, portfolio.getCash());
    assertEquals(0.0, portfolio.getReservedCash());

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
    Portfolio portfolio = portfolio(0.0, 0.0);
    when(portfolioRepository.findByPortfolioID(1)).thenReturn(portfolio);
    when(positionRepository.findByPortfolioIDAndSymbol(1, "NOVA")).thenReturn(position);

    service.applySellFill(1, "NOVA", 15, 2500);

    assertEquals(375.0, portfolio.getCash());
    assertEquals(5, position.getQuantity());
    assertEquals(0, position.getReservedQuantity());
    assertEquals(175.0, position.getRealizedPnl(), 1e-9);
    assertEquals(1, position.getLots().size());
    assertEquals(5, position.getLots().get(0).getQuantity());
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
