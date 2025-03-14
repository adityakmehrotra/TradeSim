package com.adityamehrotra.paper_trader.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
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

import com.adityamehrotra.paper_trader.model.Asset;
import com.adityamehrotra.paper_trader.model.Portfolio;
import com.adityamehrotra.paper_trader.model.SecurityModel;
import com.adityamehrotra.paper_trader.model.Transaction;
import com.adityamehrotra.paper_trader.repository.AccountRepository;
import com.adityamehrotra.paper_trader.repository.PortfolioRepository;
import com.adityamehrotra.paper_trader.repository.SecurityRepository;
import com.adityamehrotra.paper_trader.repository.TransactionRepository;
import com.adityamehrotra.paper_trader.service.PortfolioService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(PortfolioController.class)
public class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PortfolioRepository portfolioRepository;
    
    @MockBean
    private SecurityRepository securityRepository;
    
    @MockBean
    private TransactionRepository transactionRepository;
    
    @MockBean
    private AccountRepository accountRepository;
    
    @MockBean
    private PortfolioService portfolioService;

    private Portfolio testPortfolio;
    private Asset testAsset;
    private SecurityModel testSecurity;
    private Transaction testTransaction;
    private List<Integer> transactionList;
    private Map<String, Asset> assetMap;
    private Set<SecurityModel> holdings;
    private Map<String, Double> assetsAvgValue;

    @BeforeEach
    void setUp() {
        // Initialize test data
        transactionList = Arrays.asList(101, 102);
        assetMap = new HashMap<>();
        holdings = new HashSet<>();
        assetsAvgValue = new HashMap<>();
        
        testAsset = new Asset();
        testAsset.setSharesOwned(10.0);
        testAsset.setInitialCashInvestment(1000.0);
        testAsset.setGmtPurchased("1677686400000L"); // March 1, 2023
        testAsset.setInitPrice(100.0);
        
        assetMap.put("AAPL", testAsset);
        assetsAvgValue.put("AAPL", 100.0);
        
        testSecurity = new SecurityModel();
        testSecurity.setCode("AAPL");
        testSecurity.setName("Apple Inc.");
        holdings.add(testSecurity);
        
        testPortfolio = new Portfolio();
        testPortfolio.setPortfolioID(1);
        testPortfolio.setAccountID(101);
        testPortfolio.setPortfolioName("Test Portfolio");
        testPortfolio.setCashAmount(5000.0);
        testPortfolio.setInitialBalance(10000.0);
        testPortfolio.setTransactionList(transactionList);
        testPortfolio.setAssets(assetMap);
        testPortfolio.setHoldings(holdings);
        testPortfolio.setAssetsAvgValue(assetsAvgValue);
        
        testTransaction = new Transaction();
        testTransaction.setTransactionID(101);
        testTransaction.setPortfolioID(1);
        testTransaction.setSecurityCode("AAPL");
        testTransaction.setOrderType("Buy");
        testTransaction.setShareAmount(10.0);
        testTransaction.setCashAmount(1000.0);
        testTransaction.setCurrPrice(100.0);
    }

    @Test
    void testCreate() throws Exception {
        when(portfolioService.addPortfolio(any(Portfolio.class))).thenReturn(testPortfolio);

        mockMvc.perform(post("/paper_trader/portfolio/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPortfolio)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioID").value(1))
                .andExpect(jsonPath("$.portfolioName").value("Test Portfolio"))
                .andExpect(jsonPath("$.cashAmount").value(5000.0));
    }

    @Test
    void testGetAllInfo() throws Exception {
        when(portfolioRepository.findById(1)).thenReturn(Optional.of(testPortfolio));

        mockMvc.perform(get("/paper_trader/portfolio/get")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioID").value(1))
                .andExpect(jsonPath("$.portfolioName").value("Test Portfolio"))
                .andExpect(jsonPath("$.cashAmount").value(5000.0));
    }

    @Test
    void testGetAccountID() throws Exception {
        when(portfolioRepository.findById(1)).thenReturn(Optional.of(testPortfolio));

        mockMvc.perform(get("/paper_trader/portfolio/get/accountID")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("101"));
    }

    @Test
    void testGetPortfolioName() throws Exception {
        when(portfolioRepository.findById(1)).thenReturn(Optional.of(testPortfolio));

        mockMvc.perform(get("/paper_trader/portfolio/get/name")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Test Portfolio"));
    }

    @Test
    void testGetCash() throws Exception {
        when(portfolioRepository.findById(1)).thenReturn(Optional.of(testPortfolio));

        mockMvc.perform(get("/paper_trader/portfolio/get/cash")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("5000.0"));
    }

    @Test
    void testGetInitCash() throws Exception {
        when(portfolioRepository.findById(1)).thenReturn(Optional.of(testPortfolio));

        mockMvc.perform(get("/paper_trader/portfolio/get/initcash")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("10000.0"));
    }

    @Test
    void testGetTransactionList() throws Exception {
        when(portfolioRepository.findById(1)).thenReturn(Optional.of(testPortfolio));

        mockMvc.perform(get("/paper_trader/portfolio/get/transactions")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]").value(101))
                .andExpect(jsonPath("$[1]").value(102));
    }

    @Test
    void testGetAssets() throws Exception {
        when(portfolioRepository.findById(1)).thenReturn(Optional.of(testPortfolio));

        mockMvc.perform(get("/paper_trader/portfolio/get/assets")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sharesOwned").value(10.0))
                .andExpect(jsonPath("$[0].initialCashInvestment").value(1000.0));
    }

    @Test
    void testGetAssetsMap() throws Exception {
        when(portfolioRepository.findById(1)).thenReturn(Optional.of(testPortfolio));

        mockMvc.perform(get("/paper_trader/portfolio/get/assetsMap")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.AAPL.sharesOwned").value(10.0))
                .andExpect(jsonPath("$.AAPL.initialCashInvestment").value(1000.0));
    }

    @Test
    void testGetAssetsMapShares() throws Exception {
        when(portfolioRepository.findById(1)).thenReturn(Optional.of(testPortfolio));

        mockMvc.perform(get("/paper_trader/portfolio/get/assetsMap/shares")
                .param("id", "1")
                .param("code", "AAPL"))
                .andExpect(status().isOk())
                .andExpect(content().string("10.0"));
    }

    @Test
    void testGetHolding() throws Exception {
        when(portfolioRepository.findById(1)).thenReturn(Optional.of(testPortfolio));

        mockMvc.perform(get("/paper_trader/portfolio/get/holdings")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code").value("AAPL"))
                .andExpect(jsonPath("$[0].name").value("Apple Inc."));
    }

    @Test
    void testGetAssetsAvgValue() throws Exception {
        when(portfolioRepository.findById(1)).thenReturn(Optional.of(testPortfolio));

        mockMvc.perform(get("/paper_trader/portfolio/get/assets/avgValue")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.AAPL").value(100.0));
    }

    @Test
    void testAddHolding() throws Exception {
        // Setup both normal and cash security cases
        SecurityModel cashSecurity = new SecurityModel();
        cashSecurity.setCode("Cash");
        
        when(portfolioRepository.findById(1)).thenReturn(Optional.of(testPortfolio));
        when(securityRepository.findById("MSFT")).thenReturn(Optional.of(new SecurityModel("MSFT", "Microsoft Corp", null, null, null, null, null)));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(testPortfolio);

        // Test adding a regular security
        mockMvc.perform(post("/paper_trader/portfolio/add/holding")
                .param("id", "1")
                .param("code", "MSFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioID").value(1));
        
        // Test adding cash
        mockMvc.perform(post("/paper_trader/portfolio/add/holding")
                .param("id", "1")
                .param("code", "Cash"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioID").value(1));
    }

    @Test
    void testAddAsset() throws Exception {
        Asset newAsset = new Asset();
        newAsset.setSharesOwned(5.0);
        newAsset.setInitialCashInvestment(500.0);
        newAsset.setGmtPurchased("1677686400000L");
        newAsset.setInitPrice(100.0);
        
        when(portfolioRepository.findById(1)).thenReturn(Optional.of(testPortfolio));
        when(transactionRepository.findById(103)).thenReturn(Optional.of(testTransaction));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(testPortfolio);
        
        // Test adding a normal asset
        mockMvc.perform(post("/paper_trader/portfolio/add/asset")
                .param("id", "1")
                .param("code", "MSFT")
                .param("transactionID", "103")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newAsset)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioID").value(1));
        
        // Test adding cash asset
        mockMvc.perform(post("/paper_trader/portfolio/add/asset")
                .param("id", "1")
                .param("code", "Cash")
                .param("transactionID", "103")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newAsset)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioID").value(1));
    }

    @Test
    void testAddTransaction() throws Exception {
        // Setup for buy transaction
        Transaction buyTransaction = new Transaction();
        buyTransaction.setTransactionID(103);
        buyTransaction.setPortfolioID(1);
        buyTransaction.setSecurityCode("MSFT");
        buyTransaction.setOrderType("Buy");
        buyTransaction.setShareAmount(5.0);
        buyTransaction.setCashAmount(500.0);
        buyTransaction.setCurrPrice(100.0);
        
        // Setup for sell transaction
        Transaction sellTransaction = new Transaction();
        sellTransaction.setTransactionID(104);
        sellTransaction.setPortfolioID(1);
        sellTransaction.setSecurityCode("AAPL");
        sellTransaction.setOrderType("Sell");
        sellTransaction.setShareAmount(2.0);
        sellTransaction.setCashAmount(220.0);
        sellTransaction.setCurrPrice(110.0);
        
        // Setup for fund transaction
        Transaction fundTransaction = new Transaction();
        fundTransaction.setTransactionID(105);
        fundTransaction.setPortfolioID(1);
        fundTransaction.setSecurityCode("Cash");
        fundTransaction.setOrderType("Fund");
        fundTransaction.setShareAmount(0.0);
        fundTransaction.setCashAmount(1000.0);
        fundTransaction.setCurrPrice(1.0);
        
        when(portfolioRepository.findById(1)).thenReturn(Optional.of(testPortfolio));
        when(transactionRepository.findById(103)).thenReturn(Optional.of(buyTransaction));
        when(transactionRepository.findById(104)).thenReturn(Optional.of(sellTransaction));
        when(transactionRepository.findById(105)).thenReturn(Optional.of(fundTransaction));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(testPortfolio);
        
        // Test buy transaction
        mockMvc.perform(post("/paper_trader/portfolio/add/transaction")
                .param("id", "1")
                .param("transactionID", "103"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioID").value(1));
        
        // Test sell transaction
        mockMvc.perform(post("/paper_trader/portfolio/add/transaction")
                .param("id", "1")
                .param("transactionID", "104"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioID").value(1));
        
        // Test fund transaction
        mockMvc.perform(post("/paper_trader/portfolio/add/transaction")
                .param("id", "1")
                .param("transactionID", "105"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioID").value(1));
    }

    @Test
    void testGetAllPortfolios() throws Exception {
        List<Portfolio> portfolios = Arrays.asList(testPortfolio);
        when(portfolioRepository.findAll()).thenReturn(portfolios);

        mockMvc.perform(get("/paper_trader/portfolio/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].portfolioID").value(1))
                .andExpect(jsonPath("$[0].portfolioName").value("Test Portfolio"));
    }

    @Test
    void testGetNextPortfolioID() throws Exception {
        List<Portfolio> portfolios = Arrays.asList(testPortfolio);
        when(portfolioRepository.findAll()).thenReturn(portfolios);

        mockMvc.perform(get("/paper_trader/portfolio/get/nextportfolioID"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    @Test
    void testDeletePortfolio() throws Exception {
        Portfolio testPortfolio = new Portfolio();
        testPortfolio.setPortfolioID(1);
        testPortfolio.setAccountID(101);
        
        when(portfolioRepository.findById(1)).thenReturn(Optional.of(testPortfolio));
        when(accountRepository.findById(101)).thenReturn(Optional.of(new com.adityamehrotra.paper_trader.model.Account()));
        
        mockMvc.perform(delete("/paper_trader/portfolio/remove")
                .param("id", "1"))
                .andExpect(status().isOk());
    }
}