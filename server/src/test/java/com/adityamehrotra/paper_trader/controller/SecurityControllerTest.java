package com.adityamehrotra.paper_trader.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adityamehrotra.paper_trader.model.SecurityModelLegacy;
import com.adityamehrotra.paper_trader.service.SecurityService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(SecurityController.class)
public class SecurityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SecurityService securityService;

    private SecurityModelLegacy testSecurity;
    private List<SecurityModelLegacy> securityList;
    private Set<SecurityModelLegacy> securitySet;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testSecurity = new SecurityModelLegacy();
        testSecurity.setCode("AAPL");
        testSecurity.setName("Apple Inc.");
        testSecurity.setCountry("USA");
        testSecurity.setExchange("NASDAQ");
        testSecurity.setCurrency("USD");
        testSecurity.setType("Common Stock");
        testSecurity.setIsin("US0378331005");

        SecurityModelLegacy testSecurity2 = new SecurityModelLegacy();
        testSecurity2.setCode("MSFT");
        testSecurity2.setName("Microsoft Corporation");
        testSecurity2.setCountry("USA");
        testSecurity2.setExchange("NASDAQ");
        testSecurity2.setCurrency("USD");
        testSecurity2.setType("Common Stock");
        testSecurity2.setIsin("US5949181045");

        securityList = Arrays.asList(testSecurity, testSecurity2);
        securitySet = new HashSet<>(securityList);
    }

    @Test
    void testCreateOne_Success() throws Exception {
        when(securityService.createOne(any(SecurityModelLegacy.class))).thenReturn(testSecurity);

        mockMvc.perform(post("/paper_trader/security/create/one")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSecurity)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("AAPL"))
                .andExpect(jsonPath("$.name").value("Apple Inc."))
                .andExpect(jsonPath("$.country").value("USA"))
                .andExpect(jsonPath("$.exchange").value("NASDAQ"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.type").value("Common Stock"))
                .andExpect(jsonPath("$.isin").value("US0378331005"));

        verify(securityService).createOne(any(SecurityModelLegacy.class));
    }

    @Test
    void testCreateMany_Success() throws Exception {
        when(securityService.createMany(anyList())).thenReturn(securityList);

        mockMvc.perform(post("/paper_trader/security/create/many")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(securityList)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code").value("AAPL"))
                .andExpect(jsonPath("$[1].code").value("MSFT"));

        verify(securityService).createMany(anyList());
    }

    @Test
    void testGetSuggestion_Success() throws Exception {
        when(securityService.getSuggestion("app")).thenReturn(Collections.singleton(testSecurity));

        mockMvc.perform(get("/paper_trader/security/suggestion/app"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code").value("AAPL"))
                .andExpect(jsonPath("$[0].name").value("Apple Inc."));

        verify(securityService).getSuggestion("app");
    }

    @Test
    void testGetSuggestionTest_Success() throws Exception {
        when(securityService.getSuggestionTest("app")).thenReturn(Collections.singleton(testSecurity));

        mockMvc.perform(get("/paper_trader/security/suggestion/test/app"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code").value("AAPL"))
                .andExpect(jsonPath("$[0].name").value("Apple Inc."));

        verify(securityService).getSuggestionTest("app");
    }

    @Test
    void testGetAllSecurities_Success() throws Exception {
        when(securityService.getAllSecurities()).thenReturn(securityList);

        mockMvc.perform(get("/paper_trader/security/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code").value("AAPL"))
                .andExpect(jsonPath("$[1].code").value("MSFT"));

        verify(securityService).getAllSecurities();
    }

    @Test
    void testDeleteById_Success() throws Exception {
        doNothing().when(securityService).deleteById("AAPL");

        mockMvc.perform(delete("/paper_trader/security/delete/AAPL"))
                .andExpect(status().isNoContent());

        verify(securityService).deleteById("AAPL");
    }

    @Test
    void testCreateOne_InvalidInput() throws Exception {
        SecurityModelLegacy invalidSecurity = new SecurityModelLegacy();
        // Missing required fields

        mockMvc.perform(post("/paper_trader/security/create/one")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidSecurity)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(securityService);
    }

    @Test
    void testCreateMany_EmptyList() throws Exception {
        mockMvc.perform(post("/paper_trader/security/create/many")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(securityService).createMany(eq(Collections.emptyList()));
    }

    @Test
    void testGetSuggestion_NoResults() throws Exception {
        when(securityService.getSuggestion("xyz")).thenReturn(Collections.emptySet());

        mockMvc.perform(get("/paper_trader/security/suggestion/xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(securityService).getSuggestion("xyz");
    }

    @Test
    void testGetSuggestionTest_NoResults() throws Exception {
        when(securityService.getSuggestionTest("xyz")).thenReturn(Collections.emptySet());

        mockMvc.perform(get("/paper_trader/security/suggestion/test/xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(securityService).getSuggestionTest("xyz");
    }

    @Test
    void testGetAllSecurities_EmptyList() throws Exception {
        when(securityService.getAllSecurities()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/paper_trader/security/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(securityService).getAllSecurities();
    }
}