package com.adityamehrotra.paper_trader.controller;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adityamehrotra.paper_trader.model.AccountLegacy;
import com.adityamehrotra.paper_trader.model.SpecAccount;
import com.adityamehrotra.paper_trader.repository.AccountRepository;
import com.adityamehrotra.paper_trader.repository.SpecAccountRepository;
import com.adityamehrotra.paper_trader.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private SpecAccountRepository specAccountRepository;

    private AccountLegacy testAccount;
    private SpecAccount testSpecAccount;
    private List<Integer> portfolioList;

    @BeforeEach
    void setUp() {
        // Initialize test data
        portfolioList = Arrays.asList(101, 102);
        
        testAccount = new AccountLegacy();
        testAccount.setAccountID(1);
        testAccount.setFirstName("John");
        testAccount.setLastName("Doe");
        testAccount.setEmailAddress("john.doe@example.com");
        testAccount.setPassword("password123");
        testAccount.setPortfolioList(portfolioList);
        
        testSpecAccount = new SpecAccount("johndoe", "password123", 1);
    }

    @Test
    void testAddAccount() throws Exception {
        // Setup - Match the parameters used in the test
        when(accountService.addAccount(org.mockito.ArgumentMatchers.any(AccountLegacy.class), eq("johndoe"), eq("password123")))
            .thenReturn(testAccount);

        // Rest of the test remains the same
        mockMvc.perform(post("/paper_trader/account/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAccount))
                .param("username", "johndoe")
                .param("password", "password123"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountID").value(1));
                // ... rest of assertions
                
        verify(accountService).addAccount(org.mockito.ArgumentMatchers.any(AccountLegacy.class), eq("johndoe"), eq("password123"));
    }

    @Test
    void testGetAccountById() throws Exception {
        // Setup
        when(accountService.getAccount(1)).thenReturn(testAccount);

        // Execution & Verification
        mockMvc.perform(get("/paper_trader/account/get")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountID").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.emailAddress").value("john.doe@example.com"))
                .andExpect(jsonPath("$.portfolioList[0]").value(101));
                
        verify(accountService).getAccount(1);
    }
    
    @Test
    void testGetAccountById_NotFound() throws Exception {
        // Setup
        when(accountService.getAccount(99)).thenThrow(
            new NoSuchElementException("No account found with ID: 99"));

        // Execution & Verification
        mockMvc.perform(get("/paper_trader/account/get")
                .param("id", "99"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("An unexpected error occurred")));
    }

    @Test
    void testDeleteAccount() throws Exception {
        // Setup
        doNothing().when(accountRepository).deleteById(1);

        // Execution & Verification
        mockMvc.perform(delete("/paper_trader/account/delete")
                .param("id", "1"))
                .andExpect(status().isOk());
                
        verify(accountRepository).deleteById(1);
    }

    @Test
    void testGetAllAccounts() throws Exception {
        // Setup
        List<AccountLegacy> accounts = Arrays.asList(testAccount);
        when(accountRepository.findAll()).thenReturn(accounts);

        // Execution & Verification
        mockMvc.perform(get("/paper_trader/account/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].accountID").value(1))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"));
                
        verify(accountRepository).findAll();
    }

    @Test
    void testGetAllSpecAccounts() throws Exception {
        // Setup
        List<SpecAccount> specAccounts = Arrays.asList(testSpecAccount);
        when(specAccountRepository.findAll()).thenReturn(specAccounts);

        // Execution & Verification
        mockMvc.perform(get("/paper_trader/account/all/spec"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("johndoe"))
                .andExpect(jsonPath("$[0].accountID").value(1));
                
        verify(specAccountRepository).findAll();
    }

    @Test
    void testGetNextAccountID() throws Exception {
        // Setup
        when(accountRepository.findAll()).thenReturn(Arrays.asList(testAccount));

        // Execution & Verification
        mockMvc.perform(get("/paper_trader/account/get/nextAccountID"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
                
        verify(accountRepository).findAll();
    }

    @Test
    void testGetNextAccountID_EmptyRepository() throws Exception {
        // Setup
        when(accountRepository.findAll()).thenReturn(Arrays.asList());

        // Execution & Verification
        mockMvc.perform(get("/paper_trader/account/get/nextAccountID"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
                
        verify(accountRepository).findAll();
    }

    @Test
    void testAddAccount_ValidationFailure() throws Exception {
        // Create an invalid account (missing required fields)
        AccountLegacy invalidAccount = new AccountLegacy();
        invalidAccount.setAccountID(1);
        // Missing first name, last name, and email

        mockMvc.perform(post("/paper_trader/account/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidAccount))
                .param("username", "u") // too short username
                .param("password", "pwd")) // too short password
                .andExpect(status().isBadRequest());
    }
}