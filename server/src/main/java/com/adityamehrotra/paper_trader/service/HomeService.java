package com.adityamehrotra.paper_trader.service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.stereotype.Service;

/**
 * Service class to provide general health check or home endpoint functionality.
 */
@Service
public class HomeService {

    /**
     * Provides a health check message for the backend service.
     *
     * @return A welcome message indicating the backend is running.
     */
    @Operation(
            summary = "Health Check",
            description = "Returns a welcome message indicating the backend is operational."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Backend is running successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public String health() {
        return "Welcome to the TradeSim Backend";
    }
}
