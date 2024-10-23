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
import org.springframework.test.web.servlet.MvcResult;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

  private String username;

  private String password;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    account = new Account();
    account.setAccountID(1);
    account.setFirstName("John");
    account.setLastName("Doe");
    account.setEmailAddress("john.doe@example.com");
    account.setPassword("password");

    username = "john_doe";
    password = "password";
  }

  @Test
  void testAddAccount() throws Exception {
    when(accountService.addAccount(account, "john_doe", "password")).thenReturn(account);

    mockMvc.perform(post("/paper_trader/account/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("username", "john_doe")
                    .param("password", "password")
                    .content("{ \"firstName\": \"John\", \"lastName\": \"Doe\", \"emailAddress\": \"john.doe@example.com\" }"))
            .andExpect(status().isOk());
  }

  @Test
  void testGetAccountIDByUsername() throws Exception {
    Integer expectedAccountID = 1;

    when(accountService.getAccountIDByUsername(username)).thenReturn(expectedAccountID);

    MvcResult result = mockMvc.perform(get("/paper_trader/account/get/accountID_username")
                    .param("username", username))
            .andExpect(status().isOk())
            .andReturn();

    String responseContent = result.getResponse().getContentAsString();
    System.out.println("Response Content: " + responseContent);

    assertEquals(expectedAccountID.toString(), responseContent);
  }

}