package com.adityamehrotra.paper_trader.service;

import com.adityamehrotra.paper_trader.model.Portfolio;
import com.adityamehrotra.paper_trader.repository.PortfolioRepository;
import org.springframework.stereotype.Service;

@Service
public class PortfolioService {

  private final PortfolioRepository portfolioRepository;

  public PortfolioService(PortfolioRepository portfolioRepository) {
    this.portfolioRepository = portfolioRepository;
  }

  public Portfolio addPortfolio(Portfolio portfolio) {
    return portfolioRepository.save(portfolio);
  }
}
