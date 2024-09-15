package com.adityamehrotra.paper_trader.service;

import com.adityamehrotra.paper_trader.model.Account;
import com.adityamehrotra.paper_trader.model.SpecAccount;
import com.adityamehrotra.paper_trader.repository.AccountRepository;
import com.adityamehrotra.paper_trader.repository.SpecAccountRepository;
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

    public AccountService(AccountRepository accountRepository, SpecAccountRepository specAccountRepository) {
        this.accountRepository = accountRepository;
        this.specAccountRepository = specAccountRepository;
    }

    public Account addAccount(Account account, String username, String password) {
        SpecAccount specAccount = new SpecAccount(username, password, account.getAccountID());
        specAccountRepository.save(specAccount);
        return accountRepository.save(account);
    }

    public Account getAccount(Integer id) {
        return accountRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException(ID_NOT_FOUND + id));
    }

    public Integer getAccountIDByUsername(String username) {
        return specAccountRepository.findById(username)
                .map(SpecAccount::getAccountID)
                .orElseThrow(() -> new NoSuchElementException(USERNAME_NOT_FOUND + username));
    }

    public String getAccountFirstNameByID(Integer id) {
        return accountRepository.findById(id)
                .map(Account::getFirstName)
                .orElseThrow(() -> new NoSuchElementException(ID_NOT_FOUND + id));
    }

    public String getAccountLastNameByID(Integer id) {
        return accountRepository.findById(id)
                .map(Account::getLastName)
                .orElseThrow(() -> new NoSuchElementException("ID not found: " + id));
    }

    public String getEmailAddressByID(Integer id) {
        return  accountRepository.findById(id)
                .map(Account::getEmailAddress)
                .orElseThrow(() -> new NoSuchElementException("ID not found: " + id));
    }

    public String getPasswordByUsername(String username) {
        return specAccountRepository.findById(username)
                .map(SpecAccount::getPassword)
                .orElseThrow(() -> new NoSuchElementException("Username not found: " + username));
    }

    public String getPasswordByID(Integer id) {
        return accountRepository.findById(id)
            .map(Account::getPassword)
            .orElseThrow(() -> new NoSuchElementException("ID not found: " + id));
    }

    public Boolean checkUsernameExistsByUsername(String username) {
        return specAccountRepository.existsById(username);
    }

    public List<Integer> getPortfolioList(Integer id) {
        return accountRepository.findById(id)
                .map(Account::getPortfolioList)
                .orElseThrow(() -> new NoSuchElementException("ID not found: " + id));
    }

    public Account addPortfolio(Integer id, Integer portfolioID) {
        List<Integer> portfolios;
        List<Integer> portfolioList = getPortfolioList(id);

        portfolios = Objects.requireNonNullElseGet(portfolioList, ArrayList::new);
        portfolios.add(0, portfolioID);
        Account account = new Account(id, getAccountFirstNameByID(id), getAccountLastNameByID(id), getEmailAddressByID(id), getPasswordByID(id), portfolios);

        return accountRepository.save(account);
    }

    public Account deletePortfolio(Integer id, Integer portfolioID) {
        List<Integer> portfolios = getPortfolioList(id);
        portfolios.remove(portfolioID);
        Account account = new Account(id, getAccountFirstNameByID(id), getAccountLastNameByID(id), getEmailAddressByID(id), getPasswordByID(id), portfolios);
        return accountRepository.save(account);
    }
}
