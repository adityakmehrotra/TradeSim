package com.adityamehrotra.paper_trader.controller;

import com.adityamehrotra.paper_trader.model.AccountLegacy;
import com.adityamehrotra.paper_trader.model.SpecAccount;
import com.adityamehrotra.paper_trader.repository.AccountRepository;
import com.adityamehrotra.paper_trader.repository.SpecAccountRepository;
import com.adityamehrotra.paper_trader.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/paper_trader/account")
@Tag(name = "Account Management", description = "Endpoints for managing accounts, including creation, deletion, and retrieval of account details.")
@Validated
public class AccountController {

    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final SpecAccountRepository specAccountRepository;

    public AccountController(
            AccountRepository accountRepository, AccountService accountService,
            SpecAccountRepository specAccountRepository) {
        this.accountRepository = accountRepository;
        this.accountService = accountService;
        this.specAccountRepository = specAccountRepository;
    }

    @Operation(summary = "Add Account", description = "Create a new account with the specified details.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountLegacy addAccount(
            @Parameter(description = "Account object to be created", required = true) @Valid @RequestBody AccountLegacy account,
            @Parameter(description = "Username for the account", required = true) @NotNull @Size(min = 3, max = 20) @RequestParam String username,
            @Parameter(description = "Password for the account", required = true) @NotNull @Size(min = 6) @RequestParam String password) {
        return accountService.addAccount(account, username, password);
    }

    @Operation(summary = "Get Account Details by ID", description = "Retrieve account details using the account ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/get")
    public AccountLegacy getAccountById(
            @Parameter(description = "Account ID to retrieve details", required = true) @NotNull @RequestParam Integer id) {
        return accountService.getAccount(id);
    }

    @Operation(summary = "User Login", description = "Authenticate user with username and password.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Parameter(description = "Username", required = true) @NotNull @RequestParam String username,
            @Parameter(description = "Password", required = true) @NotNull @RequestParam String password) {
        try {
            SpecAccount specAccount = specAccountRepository.findById(username).orElse(null);

            if (specAccount != null && specAccount.getPassword().equals(password)) {
                AccountLegacy account = accountRepository.findById(specAccount.getAccountID()).orElse(null);

                if (account != null) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "Authentication successful");
                    response.put("account", account);
                    response.put("username", username);

                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("success", false, "message", "Account details not found"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Invalid username or password"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message",
                            "An error occurred during authentication: " + e.getMessage()));
        }
    }

    @Operation(summary = "Delete Account", description = "Delete an account using the account ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public void deleteAccount(
            @Parameter(description = "Account ID to delete", required = true) @NotNull @RequestParam Integer id) {
        accountRepository.deleteById(id);
    }

    @Operation(summary = "Get All Accounts", description = "Retrieve a list of all accounts.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/all")
    public List<AccountLegacy> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Operation(summary = "Get All Specific Accounts", description = "Retrieve a list of all specific accounts.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Specific accounts retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/all/spec")
    public List<SpecAccount> getAllSpecAccounts() {
        return specAccountRepository.findAll();
    }

    @Operation(summary = "Get Next Account ID", description = "Retrieve the next available account ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Next account ID retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/get/nextAccountID")
    public Integer getNextAccountID() {
        return accountRepository.findAll().stream()
                .max(Comparator.comparingInt(AccountLegacy::getAccountID))
                .map(account -> account.getAccountID() + 1)
                .orElse(1);
    }

    // Exception handling for validation and other errors
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralException(Exception ex) {
        return "An unexpected error occurred: " + ex.getMessage();
    }
}
