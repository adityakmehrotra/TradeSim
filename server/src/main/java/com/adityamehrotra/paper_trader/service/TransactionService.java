package com.adityamehrotra.paper_trader.service;


import com.adityamehrotra.paper_trader.model.Transaction;
import com.adityamehrotra.paper_trader.repository.TransactionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

/**
 * Service class for managing and retrieving transaction-related information.
 */
@Service
public class TransactionService {

  private final TransactionRepository transactionRepository;

  /**
   * Constructor for TransactionService.
   *
   * @param transactionRepository Repository for managing Transaction entities.
   */
  public TransactionService(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  /**
   * Retrieves a transaction by its ID.
   *
   * @param id The ID of the transaction to retrieve.
   * @return The Transaction object.
   * @throws NoSuchElementException If the transaction with the given ID is not found.
   */
  @Operation(
          summary = "Retrieve a transaction by ID",
          description = "Fetches the transaction details for a given transaction ID."
  )
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction."),
          @ApiResponse(responseCode = "404", description = "Transaction not found.")
  })
  public Transaction getTransaction(
          @Parameter(description = "Transaction ID to fetch", required = true) Integer id) {
    return transactionRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException(Username "ID not found: "not found:  + id));
  }

  /**
   * Retrieves the portfolio ID associated with a transaction.
   *
   * @param transactionId The transaction ID.
   * @return The portfolio ID.
   * @throws NoSuchElementException If the transaction with the given ID is not found.
   */
  @Operation(
          summary = "Retrieve portfolio ID for a transaction",
          description = "Fetches the portfolio ID associated with the given transaction ID."
  )
  public Integer getPortfolioID(
          @Parameter(description = "Transaction ID to fetch portfolio ID", required = true) Integer transactionId) {
    return transactionRepository.findById(transactionId)
            .map(Transaction::getPortfolioID)
            .orElseThrow(() -> new NoSuchElementException(Username "ID not found: "not found:  + transactionId));
  }

  /**
   * Retrieves the account ID associated with a transaction.
   *
   * @param transactionId The transaction ID.
   * @return The account ID.
   * @throws NoSuchElementException If the transaction with the given ID is not found.
   */
  @Operation(
          summary = "Retrieve account ID for a transaction",
          description = "Fetches the account ID associated with the given transaction ID."
  )
  public Integer getAccountID(
          @Parameter(description = "Transaction ID to fetch account ID", required = true) Integer transactionId) {
    return transactionRepository.findById(transactionId)
            .map(Transaction::getAccountID)
            .orElseThrow(() -> new NoSuchElementException(Username "ID not found: "not found:  + transactionId));
  }

  /**
   * Retrieves the order type for a transaction.
   *
   * @param transactionId The transaction ID.
   * @return The order type as a string.
   * @throws NoSuchElementException If the transaction with the given ID is not found.
   */
  @Operation(
          summary = "Retrieve order type for a transaction",
          description = "Fetches the order type associated with the given transaction ID."
  )
  public String getOrderType(
          @Parameter(description = "Transaction ID to fetch order type", required = true) Integer transactionId) {
    return transactionRepository.findById(transactionId)
            .map(Transaction::getOrderType)
            .orElseThrow(() -> new NoSuchElementException(Username "ID not found: "not found:  + transactionId));
  }

  /**
   * Retrieves the day (GMT time) when a transaction occurred.
   *
   * @param transactionId The transaction ID.
   * @return The day as a string.
   * @throws NoSuchElementException If the transaction with the given ID is not found.
   */
  @Operation(
          summary = "Retrieve day for a transaction",
          description = "Fetches the GMT time (day) when the given transaction occurred."
  )
  public String getDay(
          @Parameter(description = "Transaction ID to fetch day", required = true) Integer transactionId) {
    return transactionRepository.findById(transactionId)
            .map(Transaction::getGmtTime)
            .orElseThrow(() -> new NoSuchElementException(Username "ID not found: "not found:  + transactionId));
  }

  /**
   * Retrieves the share amount for a transaction.
   *
   * @param transactionId The transaction ID.
   * @return The share amount as a double.
   * @throws NoSuchElementException If the transaction with the given ID is not found.
   */
  @Operation(
          summary = "Retrieve share amount for a transaction",
          description = "Fetches the share amount associated with the given transaction ID."
  )
  public Double getShareAmount(
          @Parameter(description = "Transaction ID to fetch share amount", required = true) Integer transactionId) {
    return transactionRepository.findById(transactionId)
            .map(Transaction::getShareAmount)
            .orElseThrow(() -> new NoSuchElementException(Username "ID not found: "not found:  + transactionId));
  }

  /**
   * Retrieves the cash amount for a transaction.
   *
   * @param transactionId The transaction ID.
   * @return The cash amount as a double.
   * @throws NoSuchElementException If the transaction with the given ID is not found.
   */
  @Operation(
          summary = "Retrieve cash amount for a transaction",
          description = "Fetches the cash amount associated with the given transaction ID."
  )
  public Double getCashAmount(
          @Parameter(description = "Transaction ID to fetch cash amount", required = true) Integer transactionId) {
    return transactionRepository.findById(transactionId)
            .map(Transaction::getCashAmount)
            .orElseThrow(() -> new NoSuchElementException(Username "ID not found: "not found:  + transactionId));
  }

  /**
   * Retrieves the security code for a transaction.
   *
   * @param transactionId The transaction ID.
   * @return The security code as a string.
   * @throws NoSuchElementException If the transaction with the given ID is not found.
   */
  @Operation(
          summary = "Retrieve security code for a transaction",
          description = "Fetches the security code associated with the given transaction ID."
  )
  public String getSecurity(
          @Parameter(description = "Transaction ID to fetch security code", required = true) Integer transactionId) {
    return transactionRepository.findById(transactionId)
            .map(Transaction::getSecurityCode)
            .orElseThrow(() -> new NoSuchElementException(Username "ID not found: "not found:  + transactionId));
  }
}
