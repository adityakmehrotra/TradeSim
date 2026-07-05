package com.adityamehrotra.tradesim.service;

import com.adityamehrotra.tradesim.model.Lot;
import com.adityamehrotra.tradesim.model.Portfolio;
import com.adityamehrotra.tradesim.model.Position;
import com.adityamehrotra.tradesim.repository.PortfolioRepository;
import com.adityamehrotra.tradesim.repository.PositionRepository;
import java.util.Iterator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Applies fills to a portfolio's cash and FIFO positions. Cash is held in dollars, prices arrive in
 * cents, and quantities are whole shares. Buy orders reserve cash and sell orders reserve shares
 * when they are placed, so a resting order cannot be spent twice.
 */
@Service
public class PositionService {
  private final PortfolioRepository portfolioRepository;
  private final PositionRepository positionRepository;

  public PositionService(
      PortfolioRepository portfolioRepository, PositionRepository positionRepository) {
    this.portfolioRepository = portfolioRepository;
    this.positionRepository = positionRepository;
  }

  public List<Position> positionsFor(int portfolioId) {
    return positionRepository.findByPortfolioID(portfolioId);
  }

  public double availableCash(int portfolioId) {
    Portfolio portfolio = portfolioRepository.findByPortfolioID(portfolioId);
    return portfolio == null ? 0.0 : portfolio.availableCash();
  }

  private static double dollars(long priceCents, long quantity) {
    return (priceCents * quantity) / 100.0;
  }

  public boolean reserveCash(int portfolioId, long cents) {
    Portfolio portfolio = portfolioRepository.findByPortfolioID(portfolioId);
    if (portfolio == null || portfolio.availableCash() < cents / 100.0) {
      return false;
    }
    portfolio.setReservedCash(portfolio.reservedCashOrZero() + cents / 100.0);
    portfolioRepository.save(portfolio);
    return true;
  }

  public void releaseCash(int portfolioId, long cents) {
    Portfolio portfolio = portfolioRepository.findByPortfolioID(portfolioId);
    if (portfolio == null) {
      return;
    }
    portfolio.setReservedCash(Math.max(0.0, portfolio.reservedCashOrZero() - cents / 100.0));
    portfolioRepository.save(portfolio);
  }

  public boolean reserveShares(int portfolioId, String symbol, long quantity) {
    Position position = positionRepository.findByPortfolioIDAndSymbol(portfolioId, symbol);
    if (position == null || position.availableQuantity() < quantity) {
      return false;
    }
    position.setReservedQuantity(position.getReservedQuantity() + quantity);
    positionRepository.save(position);
    return true;
  }

  public void releaseShares(int portfolioId, String symbol, long quantity) {
    Position position = positionRepository.findByPortfolioIDAndSymbol(portfolioId, symbol);
    if (position == null) {
      return;
    }
    position.setReservedQuantity(Math.max(0, position.getReservedQuantity() - quantity));
    positionRepository.save(position);
  }

  public void applyBuyFill(
      int portfolioId, String symbol, long quantity, long priceCents, long reservedPerShareCents) {
    Portfolio portfolio = portfolioRepository.findByPortfolioID(portfolioId);
    Position position = getOrCreate(portfolioId, symbol);

    portfolio.setCash(portfolio.getCash() - dollars(priceCents, quantity));
    portfolio.setReservedCash(
        Math.max(0.0, portfolio.reservedCashOrZero() - dollars(reservedPerShareCents, quantity)));
    position.getLots().add(new Lot(quantity, priceCents));
    position.setQuantity(position.getQuantity() + quantity);

    portfolioRepository.save(portfolio);
    positionRepository.save(position);
  }

  public void applySellFill(int portfolioId, String symbol, long quantity, long priceCents) {
    Portfolio portfolio = portfolioRepository.findByPortfolioID(portfolioId);
    Position position = getOrCreate(portfolioId, symbol);

    double proceeds = dollars(priceCents, quantity);
    double costBasis = consumeLots(position, quantity);

    portfolio.setCash(portfolio.getCash() + proceeds);
    position.setQuantity(position.getQuantity() - quantity);
    position.setReservedQuantity(Math.max(0, position.getReservedQuantity() - quantity));
    position.setRealizedPnl(position.getRealizedPnl() + (proceeds - costBasis));

    portfolioRepository.save(portfolio);
    positionRepository.save(position);
  }

  private double consumeLots(Position position, long quantity) {
    double costBasis = 0.0;
    long remaining = quantity;
    Iterator<Lot> lots = position.getLots().iterator();
    while (remaining > 0 && lots.hasNext()) {
      Lot lot = lots.next();
      long taken = Math.min(remaining, lot.getQuantity());
      costBasis += dollars(lot.getPriceCents(), taken);
      lot.setQuantity(lot.getQuantity() - taken);
      remaining -= taken;
      if (lot.getQuantity() == 0) {
        lots.remove();
      }
    }
    return costBasis;
  }

  private Position getOrCreate(int portfolioId, String symbol) {
    Position position = positionRepository.findByPortfolioIDAndSymbol(portfolioId, symbol);
    return position != null ? position : new Position(portfolioId, symbol);
  }
}
