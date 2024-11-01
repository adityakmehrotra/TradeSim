package com.adityamehrotra.paper_trader.controller;

import com.adityamehrotra.paper_trader.model.Account;
import com.adityamehrotra.paper_trader.model.SpecAccount;
import com.adityamehrotra.paper_trader.repository.AccountRepository;
import com.adityamehrotra.paper_trader.repository.SpecAccountRepository;
import com.adityamehrotra.paper_trader.service.AccountService;
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

    @Tag(name = "Account Management", description = "Endpoints for creating new accounts and deleting existing accounts")
    @Operation(
            summary = "Add Account",
            description =
                    "Add an Account. The response is an Account object with the parameters given.")
    @PostMapping("/add")
    public Account addAccount(
            @Parameter(description = "Account object to be created", required = true) @RequestBody Account account,
            @Parameter(description = "Username of the Account to be created", required = true)
            @RequestParam String username,
            @Parameter(description = "Password of the Account to be created", required = true)
            @RequestParam String password) {
        return accountService.addAccount(account, username, password);
    }

    @Tag(name = "Account Retrieval", description = "Endpoints for retrieving account information")
    @Operation(
            summary = "Get Specific Account by Account ID",
            description =
                    "Get the information for a specific account. The response is the Account object that matches the id.")
    @GetMapping("/get")
    public Account getAllInfo(
            @Parameter(description = "ID of Account to be retrieved", required = true) @RequestParam
            Integer id) {
        return accountService.getAccount(id);
    }

    @Tag(name = "Account Retrieval", description = "Endpoints for retrieving account information")
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

    @Tag(name = "Account Information", description = "Retrieve detailed information for a specific account")
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

    @Tag(name = "Account Information", description = "Retrieve detailed information for a specific account")
    @Operation(
            summary = "Get Last Name of Account by Account ID",
            description =
                    "Get the Last Name of a specific account by the id. The response is a string of the Last Name.")
    @GetMapping("/get/lastname")
    public String getLastName(
            @Parameter(description = "ID of Account Last Name to be retrieved", required = true) @RequestParam
            Integer id) {
        return accountService.getAccountLastNameByID(id);
    }

    @Tag(name = "Account Information", description = "Retrieve detailed information for a specific account")
    @Operation(
            summary = "Get Email Address of Account by Account ID",
            description =
                    "Get the Email Address of a specific account by the id. The response is a string of the Email Address.")
    @GetMapping("/get/emailAddress")
    public String getEmailAddress(
            @Parameter(description = "ID of Account Email Address to be retrieved", required = true) @RequestParam
            Integer id) {
        return accountService.getEmailAddressByID(id);
    }

    @Tag(name = "Account Information", description = "Retrieve detailed information for a specific account")
    @Operation(
            summary = "Get Password of Account by Username of Account",
            description =
                    "Get the Password of a specific account by the username. The response is a string of the Password.")
    @GetMapping("/get/password")
    public String getPasswordByUsername(
            @Parameter(description = "Username of Account Password to be retrieved", required = true) @RequestParam
            String username) {
        return accountService.getPasswordByUsername(username);
    }

    @Tag(name = "Account Information", description = "Retrieve detailed information for a specific account")
    @Operation(
            summary = "Get Password of Account by Account ID",
            description =
                    "Get the Password of a specific account by the id. The response is a string of the Password.")
    @GetMapping("/get/password/by/id")
    public String getPasswordByID(
            @Parameter(description = "ID of Account Password to be retrieved", required = true) @RequestParam
            Integer id) {
        return accountService.getPasswordByID(id);
    }

    @Tag(name = "Account Information", description = "Retrieve detailed information for a specific account")
    @Operation(
            summary = "Check if the Username exists",
            description =
                    "Check if the username exists. The response is a boolean (True or False) if the username already exists.")
    @GetMapping("/check/username")
    public boolean checkUsername(
            @Parameter(description = "Username to check if exists or not", required = true) @RequestParam
            String username) {
        return accountService.checkUsernameExistsByUsername(username);
    }

    @Tag(name = "Account Information", description = "Retrieve detailed information for a specific account")
    @Operation(
            summary = "Get the Portfolio List",
            description =
                    "Get the Portfolio List of an Account. The response is the list of Portfolio IDs.")
    @GetMapping("/get/portfolioList")
    public List<Integer> getPortfolioList(
            @Parameter(description = "ID of Account's Portfolios to be retrieved", required = true)
            @RequestParam
            Integer id) {
        return accountService.getPortfolioList(id);
    }

    @Tag(name = "Account Details Management", description = "Endpoints for modifying and managing account details")
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
        return accountService.addPortfolio(id, portfolioID);
    }

    @Tag(name = "Account Management", description = "Endpoints for creating new accounts and deleting existing accounts")
    @Operation(
            summary = "Delete Account",
            description = "Delete an Account.")
    @DeleteMapping("/delete")
    public void deleteById(
            @Parameter(description = "ID of Account to delete", required = true) @RequestParam
            Integer id) {
        accountRepository.deleteById(id);
    }

    @Tag(name = "Account Details Management", description = "Endpoints for modifying and managing account details")
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
        return accountService.deletePortfolio(id, portfolioID);
    }

    @Tag(name = "Account Retrieval", description = "Endpoints for retrieving account information")
    @Operation(
            summary = "Get all of the Accounts",
            description =
                    "Get the List of all Accounts. The response is the list of Accounts.")
    @GetMapping("/all")
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Tag(name = "Account Retrieval", description = "Endpoints for retrieving account information")
    @Operation(
            summary = "Get all of the Specific Accounts",
            description =
                    "Get the List of all Specific Accounts. The response is the list of Specific Accounts.")
    @GetMapping("/all/spec")
    public List<SpecAccount> getAllSpecAccounts() {
        return specAccountRepository.findAll();
    }

    @Tag(name = "Next ID", description = "Get the next ID available in database for a new object")
    @Operation(
            summary = "Get the next new accountID",
            description =
                    "Get the next new Account ID which should be 1 larger than the largest current Account ID. The response is an integer of the Account ID."
    )
    @GetMapping("/get/nextAccountID")
    public Integer getNextAccountID() {
        return accountRepository.findAll().stream()
                .max(Comparator.comparingInt(Account::getAccountID))
                .map(account -> account.getAccountID() + 1)
                .orElse(1);
    }
}
