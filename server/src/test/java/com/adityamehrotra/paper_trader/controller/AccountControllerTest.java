package com.adityamehrotra.paper_trader.controller;

import com.adityamehrotra.paper_trader.model.Account;
import com.adityamehrotra.paper_trader.service.AccountService;
import com.adityamehrotra.paper_trader.repository.AccountRepository;
import com.adityamehrotra.paper_trader.repository.SpecAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AccountRepository accountRepository;

  @MockBean
  private AccountService accountService;

  @MockBean
  private SpecAccountRepository specAccountRepository;
  @Autowired
  private AccountController accountController;

  private Account account;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    account = new Account();
    account.setAccountID(accountController.getNextAccountID());
    account.setFirstName("John");
    account.setLastName("Doe");
    account.setEmailAddress("john.doe@example.com");
    account.setPassword("password");
  }

  @Test
  void testAddAccount() throws Exception {
    Account account1 = new Account();
    account1.setAccountID(accountController.getNextAccountID());
    account1.setFirstName("John");
    account1.setLastName("Doe");
    account1.setEmailAddress("john.doe@example.com");
    account1.setPassword("password");
    System.out.println("AAAKJFDLKJFL: " + account1.getAccountID());

    when(accountService.addAccount(account1, "john_doe", "password")).thenReturn(account1);

    System.out.println("AAAKJFDLKJFL: " + accountController.getNextAccountID());

    mockMvc.perform(post("/paper_trader/account/add")
            .contentType(MediaType.APPLICATION_JSON)
            .param("username", "john_doe")
            .param("password", "password")
            .content("{ \"firstName\": \"John\", \"lastName\": \"Doe\", \"emailAddress\": \"john.doe@example.com\" }"))
            .andExpect(status().isOk());
  }


}