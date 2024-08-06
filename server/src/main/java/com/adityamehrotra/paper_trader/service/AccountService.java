package com.adityamehrotra.paper_trader.service;

import com.adityamehrotra.paper_trader.model.SpecAccount;
import com.adityamehrotra.paper_trader.repository.SpecAccountRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class AccountService {

    private final SpecAccountRepository specAccountRepository;

    public AccountService(SpecAccountRepository specAccountRepository) {
        this.specAccountRepository = specAccountRepository;
    }

    public Integer getAccountIDByUsername(String username) {
        return specAccountRepository.findById(username)
                .map(SpecAccount::getAccountID)
                .orElseThrow(() -> new NoSuchElementException("Username not found: " + username));
    }
}
