package com.adityamehrotra.paper_trader.service;

import com.adityamehrotra.paper_trader.model.Account;
import com.adityamehrotra.paper_trader.model.SpecAccount;
import com.adityamehrotra.paper_trader.repository.AccountRepository;
import com.adityamehrotra.paper_trader.repository.SpecAccountRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static com.adityamehrotra.paper_trader.utils.Constants.ID_NOT_FOUND;
import static com.adityamehrotra.paper_trader.utils.Constants.USERNAME_NOT_FOUND;

@Service
public class AccountService {

    private final SpecAccountRepository specAccountRepository;
    private final AccountRepository accountRepository;

    /**
     * Constructor to initialize repositories.
     *
     * @param accountRepository      Repository for managing Account objects.
     * @param specAccountRepository  Repository for managing SpecAccount objects.
     */
    public AccountService(AccountRepository accountRepository, SpecAccountRepository specAccountRepository) {
        this.accountRepository = accountRepository;
        this.specAccountRepository = specAccountRepository;
    }

    /**
     * Adds a new Account and a corresponding SpecAccount.
     *
     * @param account  The Account to be added.
     * @param username The username for the SpecAccount.
     * @param password The password for the SpecAccount.
     * @return The saved Account object.
     */
    public Account addAccount(
            @Valid @NotNull(message = "Account cannot be null") Account account,
            @NotEmpty(message = "Username cannot be empty") String username,
            @NotEmpty(message = "Password cannot be empty") String password) {
        SpecAccount specAccount = new SpecAccount(username, password, account.getAccountID());
        specAccountRepository.save(specAccount);
        return accountRepository.save(account);
    }

    /**
     * Retrieves an Account by its ID.
     *
     * @param id The ID of the Account to retrieve.
     * @return The Account object.
     * @throws NoSuchElementException If the Account with the given ID does not exist.
     */
    public Account getAccount(
            @NotNull(message = "Account ID cannot be null") Integer id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(ID_NOT_FOUND + id));
    }

    /**
     * Retrieves the Account ID associated with a username.
     *
     * @param username The username to search for.
     * @return The associated Account ID.
     * @throws NoSuchElementException If the SpecAccount with the given username does not exist.
     */
    public Integer getAccountIDByUsername(
            @NotEmpty(message = "Username cannot be empty") String username) {
        return specAccountRepository.findById(username)
                .map(SpecAccount::getAccountID)
                .orElseThrow(() -> new NoSuchElementException(USERNAME_NOT_FOUND + username));
    }

    /**
     * Retrieves the first name of an Account by its ID.
     *
     * @param id The ID of the Account.
     * @return The first name of the Account.
     * @throws NoSuchElementException If the Account with the given ID does not exist.
     */
    public String getAccountFirstNameByID(@NotNull(message = "Account ID cannot be null") Integer id) {
        return accountRepository.findById(id)
                .map(Account::getFirstName)
                .orElseThrow(() -> new NoSuchElementException(ID_NOT_FOUND + id));
    }

    /**
     * Retrieves the last name of an Account by its ID.
     *
     * @param id The ID of the Account.
     * @return The last name of the Account.
     * @throws NoSuchElementException If the Account with the given ID does not exist.
     */
    public String getAccountLastNameByID(@NotNull(message = "Account ID cannot be null") Integer id) {
        return accountRepository.findById(id)
                .map(Account::getLastName)
                .orElseThrow(() -> new NoSuchElementException(ID_NOT_FOUND + id));
    }

    /**
     * Retrieves the email address of an Account by its ID.
     *
     * @param id The ID of the Account.
     * @return The email address of the Account.
     * @throws NoSuchElementException If the Account with the given ID does not exist.
     */
    public String getEmailAddressByID(@NotNull(message = "Account ID cannot be null") Integer id) {
        return accountRepository.findById(id)
                .map(Account::getEmailAddress)
                .orElseThrow(() -> new NoSuchElementException(ID_NOT_FOUND + id));
    }

    /**
     * Retrieves the password associated with a username.
     *
     * @param username The username to search for.
     * @return The password of the SpecAccount.
     * @throws NoSuchElementException If the SpecAccount with the given username does not exist.
     */
    public String getPasswordByUsername(@NotEmpty(message = "Username cannot be empty") String username) {
        return specAccountRepository.findById(username)
                .map(SpecAccount::getPassword)
                .orElseThrow(() -> new NoSuchElementException(USERNAME_NOT_FOUND + username));
    }

    /**
     * Retrieves the password of an Account by its ID.
     *
     * @param id The ID of the Account.
     * @return The password of the Account.
     * @throws NoSuchElementException If the Account with the given ID does not exist.
     */
    public String getPasswordByID(@NotNull(message = "Account ID cannot be null") Integer id) {
        return accountRepository.findById(id)
                .map(Account::getPassword)
                .orElseThrow(() -> new NoSuchElementException(ID_NOT_FOUND + id));
    }

    /**
     * Checks if a username exists in the system.
     *
     * @param username The username to check.
     * @return True if the username exists, false otherwise.
     */
    public Boolean checkUsernameExistsByUsername(
            @NotEmpty(message = "Username cannot be empty") String username) {
        return specAccountRepository.existsById(username);
    }

    /**
     * Retrieves the portfolio list for a given Account ID.
     *
     * @param id The ID of the Account.
     * @return The list of portfolio IDs.
     * @throws NoSuchElementException If the Account with the given ID does not exist.
     */
    public List<Integer> getPortfolioList(
            @NotNull(message = "Account ID cannot be null") Integer id) {
        return accountRepository.findById(id)
                .map(Account::getPortfolioList)
                .orElseThrow(() -> new NoSuchElementException(ID_NOT_FOUND + id));
    }

    /**
     * Adds a portfolio to an Account.
     *
     * @param id          The Account ID.
     * @param portfolioID The portfolio ID to add.
     * @return The updated Account object.
     */
    public Account addPortfolio(
            @NotNull(message = "Account ID cannot be null") Integer id,
            @NotNull(message = "Portfolio ID cannot be null") Integer portfolioID) {
        List<Integer> portfolios = Objects.requireNonNullElseGet(getPortfolioList(id), ArrayList::new);
        portfolios.add(0, portfolioID);
        Account account = new Account(
                id,
                getAccountFirstNameByID(id),
                getAccountLastNameByID(id),
                getEmailAddressByID(id),
                getPasswordByID(id),
                portfolios
        );
        return accountRepository.save(account);
    }

    /**
     * Removes a portfolio from an Account.
     *
     * @param id          The Account ID.
     * @param portfolioID The portfolio ID to remove.
     * @return The updated Account object.
     */
    public Account deletePortfolio(
            @NotNull(message = "Account ID cannot be null") Integer id,
            @NotNull(message = "Portfolio ID cannot be null") Integer portfolioID) {
        List<Integer> portfolios = getPortfolioList(id);
        portfolios.remove(portfolioID);
        Account account = new Account(
                id,
                getAccountFirstNameByID(id),
                getAccountLastNameByID(id),
                getEmailAddressByID(id),
                getPasswordByID(id),
                portfolios
        );
        return accountRepository.save(account);
    }
}
