package com.adityamehrotra.paper_trader.controller;

import com.adityamehrotra.paper_trader.model.Account;
import com.adityamehrotra.paper_trader.model.SpecAccount;
import com.adityamehrotra.paper_trader.repository.AccountRepository;
import com.adityamehrotra.paper_trader.repository.SpecAccountRepository;
import com.adityamehrotra.paper_trader.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Value;
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
@RequestMapping("/paper_trader/account")
public class AccountController {

  private final AccountRepository accountRepository;
  private final AccountService accountService;
  private final SpecAccountRepository specAccountRepository;

  public AccountController(
          AccountRepository accountRepository, AccountService accountService, SpecAccountRepository specAccountRepository) {
    this.accountRepository = accountRepository;
    this.accountService = accountService;
    this.specAccountRepository = specAccountRepository;
  }

  @Tag(name = "Post Account", description = "POST method of Account APIs")
  @PostMapping("/add")
  public Account addAccount(
          @RequestBody Account account, @RequestParam String username, @RequestParam String password) {
    setSpecAccount(username, password, account.getAccountID());
    return accountRepository.save(account);
  }

  public SpecAccount setSpecAccount(String username, String password, Integer accountID) {
    SpecAccount specAccount = new SpecAccount(username, password, accountID);
    return specAccountRepository.save(specAccount);
  }

  @Tag(name = "Get Account", description = "GET methods of Account APIs")
  @Operation(
      summary = "Get Specific Account by Account ID",
      description =
          "Get the information for a specific account. The response is the Account object that matches the id.")
  @GetMapping("/get")
  public Account getAllInfo(
      @Parameter(description = "ID of Account to be retrieved", required = true) @RequestParam
      Integer id) {
    return accountRepository.findById(id).get();
  }

  @Tag(name = "Get Account", description = "GET methods of Account APIs")
  @Operation(
      summary = "Get Account ID by Account Username",
      description =
          "Get the account ID of a specific account by the username. The response is an integer of the Account ID.")
  @GetMapping("/get/accountID_username")
  public Integer getAccountIDByUsername(
      @Parameter(description = "Username of Account ID to be retrieved", required = true) @RequestParam
      String username) {
    return accountService.getAccountIDByUsername(username);
  }

  @Tag(name = "Get Account", description = "GET methods of Account APIs")
  @Operation(
      summary = "Get First Name of Account by Account ID",
      description =
          "Get the First Name of a specific account by the id. The response is a string of the First Name.")
  @GetMapping("/get/firstname")
  public String getFirstName(
      @Parameter(description = "ID of Account First Name to be retrieved", required = true) @RequestParam
      Integer id) {
    return accountService.getAccountFirstNameByID(id);
  }

  @Tag(name = "get Account", description = "GET methods of Account APIs")
  @GetMapping("/get/lastname")
  public String getLastName(@RequestParam Integer id) {
    return accountRepository.findById(id).get().getLastName();
  }

  @Tag(name = "get Account", description = "GET methods of Account APIs")
  @GetMapping("/get/emailAddress")
  public String getEmailAddress(@RequestParam Integer id) {
    return accountRepository.findById(id).get().getEmailAddress();
  }

  @GetMapping("/get/password")
  public String getPasswordNew(@RequestParam String username) {
    if (specAccountRepository.existsById(username)) {
      return specAccountRepository.findById(username).get().getPassword();
    }
    return "This username and/or password is incorrect";
  }

  public String getPasswordById(@RequestParam Integer id) {
    return accountRepository.findById(id).get().getPassword();
  }

  @GetMapping("/check/username")
  public boolean checkUsername(@RequestParam String username) {
    return specAccountRepository.existsById(username);
  }

  @GetMapping("/get/accountID")
  public Integer getAccountID(@RequestParam String username) {
    return specAccountRepository.findById(username).get().getAccountID();
  }

  @Tag(name = "get Account", description = "GET methods of Account APIs")
  @Operation(
          summary = "Get the Portfolio List",
          description =
                  "Get the Portfolio List of an Account. The response is the list of Portfolio IDs.")
  @GetMapping("/get/portfolioList")
  public List<Integer> getPortfolioList(
          @Parameter(description = "ID of Account's Portfolios to be retrieved", required = true)
          @RequestParam
          Integer id) {
    return accountRepository.findById(id).get().getPortfolioList();
  }

  @Tag(name = "Modify Account Portfolios", description = "POST method of Account APIs")
  @Operation(
          summary = "Add Portfolio",
          description =
                  "Add a Portfolio to an Account. The response is an Account object with the portfolioID appended to the Account's list of portfolios.")
  @PostMapping("/add/portfolioList")
  public Account addPortfolio(
          @Parameter(description = "ID of Account to add Portfolio to", required = true) @RequestParam
          Integer id,
          @Parameter(
                  description = "ID of portfolio to add to Account's list of Portfolios",
                  required = true)
          @RequestParam
          Integer portfolioID) {
    List<Integer> portfolios;
    List<Integer> portfolioList = accountRepository.findById(id).get().getPortfolioList();
    if (portfolioList == null) {
      portfolios = new ArrayList<>();
    } else {
      portfolios = portfolioList;
    }
    portfolios.add(0, portfolioID);
    Account account =
            new Account(
                    id,
                    getFirstName(id),
                    getLastName(id),
                    getEmailAddress(id),
                    getPasswordById(id),
                    portfolios);
    return accountRepository.save(account);
  }

  @Tag(name = "Delete Account", description = "DELETE methods of Account APIs")
  @Operation(summary = "Delete Account", description = "Delete an Account.")
  @DeleteMapping("/delete")
  public void deleteById(
          @Parameter(description = "ID of Account to delete", required = true) @RequestParam
          Integer id) {
    accountRepository.deleteById(id);
  }

  @Tag(name = "Modify Account Portfolios", description = "DELETE methods of Account APIs")
  @Operation(
          summary = "Delete Portfolio",
          description =
                  "Delete a Portfolio from an Account. The response is an Account object with the portfolioID removed from the Account's list of portfolios.")
  @PostMapping("/delete/portfolioList")
  public Account deletePortfolio(
          @Parameter(description = "ID of Account to remove Portfolio from", required = true)
          @RequestParam
          Integer id,
          @Parameter(
                  description = "ID of portfolio to remove from Account's list of Portfolios",
                  required = true)
          @RequestParam
          Integer portfolioID) {
    List<Integer> portfolios = accountRepository.findById(id).get().getPortfolioList();
    System.out.println(portfolios);
    portfolios.remove(portfolioID);
    System.out.println(portfolios);
    Account account =
            new Account(
                    id,
                    getFirstName(id),
                    getLastName(id),
                    getEmailAddress(id),
                    getPasswordById(id),
                    portfolios);
    return accountRepository.save(account);
  }

  @Tag(name = "get Account", description = "GET methods of Account APIs")
  @GetMapping("/all")
  public List<Account> getAllAuthors() {
    System.out.println("HI");
    return accountRepository.findAll();
  }

  @Tag(name = "get Account", description = "GET methods of Account APIs")
  @GetMapping("/all/spec")
  public List<SpecAccount> getAllAuthorsTemp() {
    return specAccountRepository.findAll();
  }

  @GetMapping("/get/nextAccountID")
  public Integer getNextAccountID() {
    return accountRepository.findAll().stream()
            .max(Comparator.comparingInt(Account::getAccountID))
            .map(account -> account.getAccountID() + 1)
            .orElse(1);
  }
}
