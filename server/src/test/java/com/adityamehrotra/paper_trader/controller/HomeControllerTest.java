package com.adityamehrotra.paper_trader.controller;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adityamehrotra.paper_trader.service.HomeService;

@WebMvcTest(HomeController.class)
public class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HomeService homeService;

    @Test
    void testHealth() throws Exception {
        // Arrange
        String expectedResponse = "API is healthy and running!";
        when(homeService.health()).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    void testHealthWithDifferentResponse() throws Exception {
        // Arrange
        String expectedResponse = "System status: operational";
        when(homeService.health()).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    void testHealthWithEmptyResponse() throws Exception {
        // Arrange
        String expectedResponse = "";
        when(homeService.health()).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }
}