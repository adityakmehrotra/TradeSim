package com.adityamehrotra.paper_trader.controller;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

import com.adityamehrotra.paper_trader.model.Transaction;
import com.adityamehrotra.paper_trader.repository.TransactionRepository;
import com.adityamehrotra.paper_trader.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private TransactionService transactionService;

    private Transaction testTransaction1;
    private Transaction testTransaction2;
    private List<Transaction> transactionList;

    @BeforeEach
    void setUp() {
        // Create test transaction 1 (Buy)
        testTransaction1 = new Transaction();
        testTransaction1.setTransactionID(1);
        testTransaction1.setPortfolioID(101);
        testTransaction1.setAccountID(201);
        testTransaction1.setOrderType("Buy");
        testTransaction1.setSecurityCode("AAPL");
        testTransaction1.setGmtTime("2023-01-15T10:30:00Z");
        testTransaction1.setShareAmount(10.0);
        testTransaction1.setCashAmount(1500.0);
        testTransaction1.setCurrPrice(150.0);

        // Create test transaction 2 (Sell)
        testTransaction2 = new Transaction();
        testTransaction2.setTransactionID(2);
        testTransaction2.setPortfolioID(102);
        testTransaction2.setAccountID(202);
        testTransaction2.setOrderType("Sell");
        testTransaction2.setSecurityCode("MSFT");
        testTransaction2.setGmtTime("2023-01-16T14:45:00Z");
        testTransaction2.setShareAmount(5.0);
        testTransaction2.setCashAmount(1750.0);
        testTransaction2.setCurrPrice(350.0);

        transactionList = Arrays.asList(testTransaction1, testTransaction2);
    }

    @Test
    void testCreateTransaction() throws Exception {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction1);

        mockMvc.perform(post("/paper_trader/transaction/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTransaction1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionID").value(1))
                .andExpect(jsonPath("$.portfolioID").value(101))
                .andExpect(jsonPath("$.accountID").value(201))
                .andExpect(jsonPath("$.orderType").value("Buy"))
                .andExpect(jsonPath("$.securityCode").value("AAPL"))
                .andExpect(jsonPath("$.gmtTime").value("2023-01-15T10:30:00Z"))
                .andExpect(jsonPath("$.shareAmount").value(10.0))
                .andExpect(jsonPath("$.cashAmount").value(1500.0))
                .andExpect(jsonPath("$.currPrice").value(150.0));

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testGetAllInfo() throws Exception {
        when(transactionService.getTransaction(1)).thenReturn(testTransaction1);

        mockMvc.perform(get("/paper_trader/transaction/get")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionID").value(1))
                .andExpect(jsonPath("$.orderType").value("Buy"))
                .andExpect(jsonPath("$.securityCode").value("AAPL"));

        verify(transactionService).getTransaction(1);
    }

    @Test
    void testGetAllInfo_NotFound() throws Exception {
        when(transactionService.getTransaction(99)).thenThrow(new NoSuchElementException("ID not found: 99"));

        mockMvc.perform(get("/paper_trader/transaction/get")
                .param("id", "99"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetPortfolioID() throws Exception {
        when(transactionService.getPortfolioID(1)).thenReturn(101);

        mockMvc.perform(get("/paper_trader/transaction/get/portfolioID")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("101"));

        verify(transactionService).getPortfolioID(1);
    }

    @Test
    void testGetAccountID() throws Exception {
        when(transactionService.getAccountID(1)).thenReturn(201);

        mockMvc.perform(get("/paper_trader/transaction/get/accountID")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("201"));

        verify(transactionService).getAccountID(1);
    }

    @Test
    void testGetOrderType() throws Exception {
        when(transactionService.getOrderType(1)).thenReturn("Buy");

        mockMvc.perform(get("/paper_trader/transaction/get/ordertype")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Buy"));

        verify(transactionService).getOrderType(1);
    }

    @Test
    void testGetDay() throws Exception {
        when(transactionService.getDay(1)).thenReturn("2023-01-15T10:30:00Z");

        mockMvc.perform(get("/paper_trader/transaction/get/gmtTime")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("2023-01-15T10:30:00Z"));

        verify(transactionService).getDay(1);
    }

    @Test
    void testGetShareAmount() throws Exception {
        when(transactionService.getShareAmount(1)).thenReturn(10.0);

        mockMvc.perform(get("/paper_trader/transaction/get/shareamount")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("10.0"));

        verify(transactionService).getShareAmount(1);
    }

    @Test
    void testGetCashAmount() throws Exception {
        when(transactionService.getCashAmount(1)).thenReturn(1500.0);

        mockMvc.perform(get("/paper_trader/transaction/get/cashamount")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("1500.0"));

        verify(transactionService).getCashAmount(1);
    }

    @Test
    void testGetSecurity() throws Exception {
        when(transactionService.getSecurity(1)).thenReturn("AAPL");

        mockMvc.perform(get("/paper_trader/transaction/get/security")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("AAPL"));

        verify(transactionService).getSecurity(1);
    }

    @Test
    void testGetAllTransactions() throws Exception {
        when(transactionRepository.findAll()).thenReturn(transactionList);

        mockMvc.perform(get("/paper_trader/transaction/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].transactionID").value(1))
                .andExpect(jsonPath("$[0].securityCode").value("AAPL"))
                .andExpect(jsonPath("$[1].transactionID").value(2))
                .andExpect(jsonPath("$[1].securityCode").value("MSFT"));

        verify(transactionRepository).findAll();
    }

    @Test
    void testDeleteById() throws Exception {
        doNothing().when(transactionRepository).deleteById(1);

        mockMvc.perform(delete("/paper_trader/transaction/delete")
                .param("id", "1"))
                .andExpect(status().isOk());

        verify(transactionRepository).deleteById(1);
    }

    @Test
    void testGetNextID() throws Exception {
        when(transactionRepository.findAll()).thenReturn(transactionList);

        mockMvc.perform(get("/paper_trader/transaction/id"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(transactionRepository, times(2)).findAll();
    }

    @Test
    void testGetNextTransactionID() throws Exception {
        when(transactionRepository.findAll()).thenReturn(transactionList);

        mockMvc.perform(get("/paper_trader/transaction/get/nextTransactionID"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(transactionRepository).findAll();
    }

    @Test
    void testGetNextTransactionID_EmptyRepository() throws Exception {
        when(transactionRepository.findAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/paper_trader/transaction/get/nextTransactionID"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(transactionRepository).findAll();
    }
    
    @Test
    void testCreateTransaction_ValidationErrors() throws Exception {
        Transaction invalidTransaction = new Transaction();
        // Missing required fields
        
        mockMvc.perform(post("/paper_trader/transaction/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTransaction)))
                .andExpect(status().isBadRequest());
                
        verifyNoInteractions(transactionRepository);
    }
}