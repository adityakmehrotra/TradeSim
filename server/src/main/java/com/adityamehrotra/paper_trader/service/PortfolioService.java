package com.adityamehrotra.paper_trader.service;

import com.adityamehrotra.paper_trader.dto.PortfolioRequest;
import com.adityamehrotra.paper_trader.model.Portfolio;
import com.adityamehrotra.paper_trader.model.User;
import com.adityamehrotra.paper_trader.repository.PortfolioRepository;
import com.adityamehrotra.paper_trader.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
