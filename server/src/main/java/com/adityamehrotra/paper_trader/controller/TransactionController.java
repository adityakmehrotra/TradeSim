package com.adityamehrotra.paper_trader.controller;

import com.adityamehrotra.paper_trader.model.Transaction;
import com.adityamehrotra.paper_trader.repository.TransactionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/paper_trader/transaction")
public class TransactionController {
  private final TransactionRepository transactionRepository;

  public TransactionController(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  @PostMapping("/create")
  public Transaction create(@RequestBody Transaction transaction) {
    return transactionRepository.save(transaction);
  }

  @Tag(name = "Get Transaction", description = "GET methods of Transaction APIs")
  @Operation(
          summary = "Get Specific Transaction",
          description =
                  "Get the information for a specific transaction. The response is the Transaction object that matches the id.")
  @GetMapping("/get")
  public Transaction getAllInfo(
          @Parameter(description = "ID of Transaction to be retrieved", required = true) @RequestParam
          Integer id) {
    return transactionRepository.findById(id).get();
  }

  @Tag(name = "Get Transaction", description = "GET methods of Transaction APIs")
  @Operation(
          summary = "Get the Portfolio ID",
          description =
                  "Get the Portfolio ID for a specific transaction. The response is an integer of the Portfolio ID.")
  @GetMapping("/get/portfolioID")
  public Integer getPortfolioID(
          @Parameter(
                  description = "ID of Transaction whose Portfolio ID needs to be retrieved",
                  required = true)
          @RequestParam
          Integer id) {
    return transactionRepository.findById(id).get().getPortfolioID();
  }

  @Tag(name = "Get Transaction", description = "GET methods of Transaction APIs")
  @Operation(
          summary = "Get the Account ID",
          description =
                  "Get the Account ID for a specific transaction. The response is an integer of the Account ID.")
  @GetMapping("/get/accountID")
  public Integer getAccountID(
          @Parameter(
                  description = "ID of Transaction whose Account ID needs to be retrieved",
                  required = true)
          @RequestParam
          Integer id) {
    return transactionRepository.findById(id).get().getAccountID();
  }

  @Tag(name = "Get Transaction", description = "GET methods of Transaction APIs")
  @Operation(
          summary = "Get the Order Type",
          description =
                  "Get the Order Type for a specific transaction. The response is a string of the order type of the transaction.")
  @GetMapping("/get/ordertype")
  public String getOrderType(
          @Parameter(
                  description = "ID of Transaction whose Order Type needs to be retrieved",
                  required = true)
          @RequestParam
          Integer id) {
    return transactionRepository.findById(id).get().getOrderType();
  }

  @Tag(name = "Get Transaction", description = "GET methods of Transaction APIs")
  @Operation(
          summary = "Get the Day",
          description =
                  "Get the Day a specific transaction was completed. The response is a string of the day when the transaction was completed.")
  @GetMapping("/get/gmtTime")
  public String getDay(
          @Parameter(
                  description = "ID of Transaction whose Order Type needs to be retrieved",
                  required = true)
          @RequestParam
          Integer id) {
    return transactionRepository.findById(id).get().getGmtTime();
  }

  @Tag(name = "Get Transaction", description = "GET methods of Transaction APIs")
  @Operation(
          summary = "Get the Share Amount",
          description =
                  "Get the Share Amount of a specific transaction. The response is a double of the share amount of the transaction.")
  @GetMapping("/get/shareamount")
  public Double getShareAmount(
          @Parameter(
                  description = "ID of Transaction whose Order Type needs to be retrieved",
                  required = true)
          @RequestParam
          Integer id) {
    return transactionRepository.findById(id).get().getShareAmount();
  }

  @Tag(name = "Get Transaction", description = "GET methods of Transaction APIs")
  @Operation(
          summary = "Get the Cash Amount",
          description =
                  "Get the Cash Amount of a specific transaction. The response is a double of the cash amount of the transaction.")
  @GetMapping("/get/cashamount")
  public Double getCashAmount(
          @Parameter(
                  description = "ID of Transaction whose Order Type needs to be retrieved",
                  required = true)
          @RequestParam
          Integer id) {
    return transactionRepository.findById(id).get().getCashAmount();
  }

  @Tag(name = "Get Transaction", description = "GET methods of Transaction APIs")
  @Operation(
          summary = "Get the Security Object",
          description =
                  "Get the Security Object correlated to a specific transaction. The response is the security object correlated to a transaction.")
  @GetMapping("/get/security")
  public String getSecurity(
          @Parameter(
                  description = "ID of Transaction whose Order Type needs to be retrieved",
                  required = true)
          @RequestParam
          Integer id) {
    return transactionRepository.findById(id).get().getSecurityCode();
  }

  @GetMapping("/all")
  public List<Transaction> getAllTransactions() {
    return transactionRepository.findAll();
  }

  @Tag(
          name = "Modify Transaction",
          description = "Transaction Modifying methods of Transaction APIs")
  @Operation(summary = "Delete Transaction", description = "Delete a Transaction.")
  @DeleteMapping("/delete")
  public void deleteById(
          @Parameter(description = "ID of Transaction to delete", required = true) @RequestParam
          Integer id) {
    transactionRepository.deleteById(id);
  }

  @GetMapping("/id")
  public Integer getNextID() {
    return transactionRepository
            .findAll()
            .get(transactionRepository.findAll().size() - 1)
            .getTransactionID()
            + 1;
  }

  @GetMapping("/get/nextTransactionID")
  public Integer getNextTransactionID() {
    return transactionRepository.findAll().stream()
            .max(Comparator.comparingInt(Transaction::getTransactionID))
            .map(account -> account.getTransactionID() + 1)
            .orElse(1);
  }
}
