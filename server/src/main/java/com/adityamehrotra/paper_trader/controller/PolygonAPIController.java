package com.adityamehrotra.paper_trader.controller;

import com.adityamehrotra.paper_trader.service.PolygonAPIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RequestMapping("/paper_trader/polygon")
@RestController
@Validated
@Tag(name = "Polygon API", description = "Endpoints for fetching stock and company data from the Polygon API.")
public class PolygonAPIController {

  private final PolygonAPIService polygonAPIService;

  public PolygonAPIController(PolygonAPIService polygonAPIService) {
    this.polygonAPIService = polygonAPIService;
  }

  @Operation(summary = "Get Current Price", description = "Fetch the current price of a stock by its ticker symbol.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Successfully fetched current price"),
          @ApiResponse(responseCode = "400", description = "Invalid ticker symbol provided"),
          @ApiResponse(responseCode = "500", description = "Error fetching current price")
  })
  @GetMapping("/price")
  public ResponseEntity<String> getCurrentPrice(
          @Parameter(description = "Stock ticker symbol", required = true)
          @RequestParam @NotNull @Size(min = 1, max = 10) String ticker) {
    try {
      return polygonAPIService.getCurrentPrice(ticker);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error fetching current price: " + e.getMessage());
    }
  }

  @Operation(summary = "Get Current News", description = "Fetch the latest news for a stock by its ticker symbol.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Successfully fetched news"),
          @ApiResponse(responseCode = "400", description = "Invalid ticker symbol provided"),
          @ApiResponse(responseCode = "500", description = "Error fetching news")
  })
  @GetMapping("/news")
  public ResponseEntity<String> getCurrentNews(
          @Parameter(description = "Stock ticker symbol", required = true)
          @RequestParam @NotNull @Size(min = 1, max = 10) String ticker) {
    try {
      return polygonAPIService.getCurrentNews(ticker);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error fetching current news: " + e.getMessage());
    }
  }

  @Operation(summary = "Get Key Statistics", description = "Fetch key statistics for a stock by its ticker symbol.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Successfully fetched key statistics"),
          @ApiResponse(responseCode = "400", description = "Invalid ticker symbol provided"),
          @ApiResponse(responseCode = "500", description = "Error fetching key statistics")
  })
  @GetMapping("/keyStatistics")
  public ResponseEntity<String> getKeyStatistics(
          @Parameter(description = "Stock ticker symbol", required = true)
          @RequestParam @NotNull @Size(min = 1, max = 10) String ticker) {
    try {
      return polygonAPIService.getKeyStatistics(ticker);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error fetching key statistics: " + e.getMessage());
    }
  }

  @Operation(summary = "Get Company Data", description = "Fetch company data for a stock by its ticker symbol.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Successfully fetched company data"),
          @ApiResponse(responseCode = "400", description = "Invalid ticker symbol provided"),
          @ApiResponse(responseCode = "500", description = "Error fetching company data")
  })
  @GetMapping("/companyData")
  public ResponseEntity<String> getCompanyData(
          @Parameter(description = "Stock ticker symbol", required = true)
          @RequestParam @NotNull @Size(min = 1, max = 10) String ticker) {
    try {
      return polygonAPIService.getCompanyData(ticker);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error fetching company data: " + e.getMessage());
    }
  }

  @Operation(summary = "Get Chart Data", description = "Fetch chart data for a stock based on specified parameters.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Successfully fetched chart data"),
          @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
          @ApiResponse(responseCode = "500", description = "Error fetching chart data")
  })
  @GetMapping("/chart")
  public ResponseEntity<String> getChart(
          @Parameter(description = "Stock ticker symbol", required = true)
          @RequestParam @NotNull @Size(min = 1, max = 10) String ticker,
          @Parameter(description = "Multiplier for aggregation", required = true)
          @RequestParam @NotNull Integer multiplier,
          @Parameter(description = "Timespan for the chart (e.g., day, week)", required = true)
          @RequestParam @NotNull @Size(min = 1, max = 20) String timespan,
          @Parameter(description = "Start date for the chart in yyyy-MM-dd format", required = true)
          @RequestParam @NotNull @Size(min = 10, max = 10) String from,
          @Parameter(description = "End date for the chart in yyyy-MM-dd format", required = true)
          @RequestParam @NotNull @Size(min = 10, max = 10) String to) {
    try {
      return polygonAPIService.getChart(ticker, multiplier, timespan, from, to);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error fetching chart data: " + e.getMessage());
    }
  }

  // Centralized Exception Handling
  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String handleIllegalArgumentException(IllegalArgumentException ex) {
    return "Invalid request: " + ex.getMessage();
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public String handleGeneralException(Exception ex) {
    return "An unexpected error occurred: " + ex.getMessage();
  }
}
