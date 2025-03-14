package com.adityamehrotra.paper_trader.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adityamehrotra.paper_trader.service.PolygonAPIService;

@WebMvcTest(PolygonAPIController.class)
public class PolygonAPIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PolygonAPIService polygonAPIService;

    private final String sampleJsonResponse = "{\"status\":\"OK\",\"data\":{\"sample\":\"data\"}}";

    @BeforeEach
    void setUp() {
        // Default successful response for all API calls
        ResponseEntity<String> successResponse = new ResponseEntity<>(sampleJsonResponse, HttpStatus.OK);
        
        // Mock service methods with default responses
        when(polygonAPIService.getCurrentPrice(anyString())).thenReturn(successResponse);
        when(polygonAPIService.getCurrentNews(anyString())).thenReturn(successResponse);
        when(polygonAPIService.getKeyStatistics(anyString())).thenReturn(successResponse);
        when(polygonAPIService.getCompanyData(anyString())).thenReturn(successResponse);
        when(polygonAPIService.getChart(anyString(), anyInt(), anyString(), anyString(), anyString()))
            .thenReturn(successResponse);
    }

    @Test
    void testGetCurrentPrice_Success() throws Exception {
        mockMvc.perform(get("/paper_trader/polygon/price")
                .param("ticker", "AAPL"))
                .andExpect(status().isOk())
                .andExpect(content().string(sampleJsonResponse));
    }

    @Test
    void testGetCurrentPrice_Error() throws Exception {
        when(polygonAPIService.getCurrentPrice("AAPL"))
            .thenThrow(new RuntimeException("API connection failed"));

        mockMvc.perform(get("/paper_trader/polygon/price")
                .param("ticker", "AAPL"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error fetching current price: API connection failed"));
    }

    @Test
    void testGetCurrentPrice_InvalidInput() throws Exception {
        mockMvc.perform(get("/paper_trader/polygon/price")
                .param("ticker", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCurrentNews_Success() throws Exception {
        mockMvc.perform(get("/paper_trader/polygon/news")
                .param("ticker", "AAPL"))
                .andExpect(status().isOk())
                .andExpect(content().string(sampleJsonResponse));
    }

    @Test
    void testGetCurrentNews_Error() throws Exception {
        when(polygonAPIService.getCurrentNews("AAPL"))
            .thenThrow(new RuntimeException("API connection failed"));

        mockMvc.perform(get("/paper_trader/polygon/news")
                .param("ticker", "AAPL"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error fetching current news: API connection failed"));
    }

    @Test
    void testGetCurrentNews_InvalidInput() throws Exception {
        mockMvc.perform(get("/paper_trader/polygon/news")
                .param("ticker", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetKeyStatistics_Success() throws Exception {
        mockMvc.perform(get("/paper_trader/polygon/keyStatistics")
                .param("ticker", "AAPL"))
                .andExpect(status().isOk())
                .andExpect(content().string(sampleJsonResponse));
    }

    @Test
    void testGetKeyStatistics_Error() throws Exception {
        when(polygonAPIService.getKeyStatistics("AAPL"))
            .thenThrow(new RuntimeException("API connection failed"));

        mockMvc.perform(get("/paper_trader/polygon/keyStatistics")
                .param("ticker", "AAPL"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error fetching key statistics: API connection failed"));
    }

    @Test
    void testGetKeyStatistics_InvalidInput() throws Exception {
        mockMvc.perform(get("/paper_trader/polygon/keyStatistics")
                .param("ticker", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCompanyData_Success() throws Exception {
        mockMvc.perform(get("/paper_trader/polygon/companyData")
                .param("ticker", "AAPL"))
                .andExpect(status().isOk())
                .andExpect(content().string(sampleJsonResponse));
    }

    @Test
    void testGetCompanyData_Error() throws Exception {
        when(polygonAPIService.getCompanyData("AAPL"))
            .thenThrow(new RuntimeException("API connection failed"));

        mockMvc.perform(get("/paper_trader/polygon/companyData")
                .param("ticker", "AAPL"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error fetching company data: API connection failed"));
    }

    @Test
    void testGetCompanyData_InvalidInput() throws Exception {
        mockMvc.perform(get("/paper_trader/polygon/companyData")
                .param("ticker", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetChart_Success() throws Exception {
        mockMvc.perform(get("/paper_trader/polygon/chart")
                .param("ticker", "AAPL")
                .param("multiplier", "1")
                .param("timespan", "day")
                .param("from", "2023-01-01")
                .param("to", "2023-01-31"))
                .andExpect(status().isOk())
                .andExpect(content().string(sampleJsonResponse));
    }

    @Test
    void testGetChart_Error() throws Exception {
        when(polygonAPIService.getChart(eq("AAPL"), eq(1), eq("day"), eq("2023-01-01"), eq("2023-01-31")))
            .thenThrow(new RuntimeException("API connection failed"));

        mockMvc.perform(get("/paper_trader/polygon/chart")
                .param("ticker", "AAPL")
                .param("multiplier", "1")
                .param("timespan", "day")
                .param("from", "2023-01-01")
                .param("to", "2023-01-31"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error fetching chart data: API connection failed"));
    }

    @Test
    void testGetChart_InvalidTicker() throws Exception {
        mockMvc.perform(get("/paper_trader/polygon/chart")
                .param("ticker", "")
                .param("multiplier", "1")
                .param("timespan", "day")
                .param("from", "2023-01-01")
                .param("to", "2023-01-31"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetChart_InvalidTimespan() throws Exception {
        mockMvc.perform(get("/paper_trader/polygon/chart")
                .param("ticker", "AAPL")
                .param("multiplier", "1")
                .param("timespan", "")
                .param("from", "2023-01-01")
                .param("to", "2023-01-31"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetChart_InvalidDateFormat() throws Exception {
        mockMvc.perform(get("/paper_trader/polygon/chart")
                .param("ticker", "AAPL")
                .param("multiplier", "1")
                .param("timespan", "day")
                .param("from", "2023/01/01") // Wrong format
                .param("to", "2023-01-31"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetChart_MissingParameter() throws Exception {
        mockMvc.perform(get("/paper_trader/polygon/chart")
                .param("ticker", "AAPL")
                .param("multiplier", "1")
                .param("timespan", "day")
                // Missing "from" parameter
                .param("to", "2023-01-31"))
                .andExpect(status().isBadRequest());
    }
}