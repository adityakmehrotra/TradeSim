package com.adityamehrotra.paper_trader.service;

import static com.adityamehrotra.paper_trader.utils.Constants.ID_NOT_FOUND;

import com.adityamehrotra.paper_trader.model.Transaction;
import com.adityamehrotra.paper_trader.repository.TransactionRepository;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {
  private final TransactionRepository transactionRepository;

  public TransactionService(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public Transaction getTransaction(Integer id) {
    return transactionRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException(ID_NOT_FOUND + id));
  }

  public Integer getPortfolioID(Integer transactionId) {
    return transactionRepository.findById(transactionId)
        .map(Transaction::getPortfolioID)
        .orElseThrow(() -> new NoSuchElementException(ID_NOT_FOUND + transactionId));
  }

  public Integer getAccountID(Integer transactionId) {
    return transactionRepository.findById(transactionId)
        .map(Transaction::getAccountID)
        .orElseThrow(() -> new NoSuchElementException(ID_NOT_FOUND + transactionId));
  }

  public String getOrderType(Integer transactionId) {
    return transactionRepository.findById(transactionId)
        .map(Transaction::getOrderType)
        .orElseThrow(() -> new NoSuchElementException(ID_NOT_FOUND + transactionId));
  }

  public String getDay(Integer transactionId) {
    return transactionRepository.findById(transactionId)
        .map(Transaction::getGmtTime)
        .orElseThrow(() -> new NoSuchElementException(ID_NOT_FOUND + transactionId));
  }

  public Double getShareAmount(Integer transactionId) {
    return transactionRepository.findById(transactionId)
        .map(Transaction::getShareAmount)
        .orElseThrow(() -> new NoSuchElementException(ID_NOT_FOUND + transactionId));
  }

  public Double getCashAmount(Integer transactionId) {
    return transactionRepository.findById(transactionId)
        .map(Transaction::getCashAmount)
        .orElseThrow(() -> new NoSuchElementException(ID_NOT_FOUND + transactionId));
  }

  public String getSecurity(Integer transactionId) {
    return transactionRepository.findById(transactionId)
        .map(Transaction::getSecurityCode)
        .orElseThrow(() -> new NoSuchElementException(ID_NOT_FOUND + transactionId));
  }
}
