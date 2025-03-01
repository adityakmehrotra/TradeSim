package com.adityamehrotra.paper_trader.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * Service class for interacting with the Polygon.io API to fetch stock-related data.
 */
@Service
public class PolygonAPIService {

    @Value("${apiKey}")
    private String polygonApiKey;

    private final RestTemplate restTemplate;

    /**
     * Constructor for PolygonAPIService.
     *
     * @param restTemplate The RestTemplate instance for making HTTP requests.
     */
    public PolygonAPIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches the current price for a given stock ticker.
     *
     * @param ticker The stock ticker symbol.
     * @return A ResponseEntity containing the current price data as a String.
     */
    @Operation(
            summary = "Get Current Price",
            description = "Fetches the current price of the given stock ticker."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved current price."),
            @ApiResponse(responseCode = "400", description = "Invalid ticker provided."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<String> getCurrentPrice(
            @Parameter(description = "Stock ticker symbol", required = true) String ticker) {
        String url = String.format(
                "https://api.polygon.io/v2/snapshot/locale/us/markets/stocks/tickers/%s?apiKey=%s",
                ticker, polygonApiKey);
        return restTemplate.getForEntity(url, String.class);
    }

    /**
     * Fetches the latest news for a given stock ticker.
     *
     * @param ticker The stock ticker symbol.
     * @return A ResponseEntity containing the news data as a String.
     */
    @Operation(
            summary = "Get Current News",
            description = "Fetches the latest news articles related to the given stock ticker."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved news articles."),
            @ApiResponse(responseCode = "400", description = "Invalid ticker provided."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<String> getCurrentNews(
            @Parameter(description = "Stock ticker symbol", required = true) String ticker) {
        String url = String.format(
                "https://api.polygon.io/v2/reference/news?ticker=%s&order=desc&limit=50&apiKey=%s",
                ticker, polygonApiKey);
        return restTemplate.getForEntity(url, String.class);
    }

        /**
         * Fetches key statistics for a given stock ticker.
         *
         * @param ticker The stock ticker symbol.
         * @return A ResponseEntity containing the key statistics data as a String.
         */
        @Operation(
                summary = "Get Key Statistics",
                description = "Fetches key statistical data for the given stock ticker."
        )
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Successfully retrieved key statistics."),
                @ApiResponse(responseCode = "400", description = "Invalid ticker provided."),
                @ApiResponse(responseCode = "500", description = "Internal server error.")
        })
        public ResponseEntity<String> getKeyStatistics(
                @Parameter(description = "Stock ticker symbol", required = true) String ticker) {
                String url = String.format(
                        "https://api.polygon.io/v2/aggs/ticker/%s/prev?adjusted=true",
                        ticker);
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + polygonApiKey);
                
                HttpEntity<String> entity = new HttpEntity<>(headers);
                
                return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        }

    /**
     * Fetches company data for a given stock ticker.
     *
     * @param ticker The stock ticker symbol.
     * @return A ResponseEntity containing the company data as a String.
     */
    @Operation(
            summary = "Get Company Data",
            description = "Fetches company-related information for the given stock ticker."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved company data."),
            @ApiResponse(responseCode = "400", description = "Invalid ticker provided."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<String> getCompanyData(
            @Parameter(description = "Stock ticker symbol", required = true) String ticker) {
        String url = String.format(
                "https://api.polygon.io/v3/reference/tickers/%s?apiKey=%s", ticker, polygonApiKey);
        return restTemplate.getForEntity(url, String.class);
    }

    /**
     * Fetches chart data for a given stock ticker over a specified time range.
     *
     * @param ticker     The stock ticker symbol.
     * @param multiplier The time multiplier for the aggregation.
     * @param timespan   The time span for the aggregation (e.g., day, minute).
     * @param from       The start date in YYYY-MM-DD format.
     * @param to         The end date in YYYY-MM-DD format.
     * @return A ResponseEntity containing the chart data as a String.
     */
    @Operation(
            summary = "Get Chart Data",
            description = "Fetches historical chart data for the given stock ticker."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved chart data."),
            @ApiResponse(responseCode = "400", description = "Invalid parameters provided."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<String> getChart(
            @Parameter(description = "Stock ticker symbol", required = true) String ticker,
            @Parameter(description = "Time multiplier for aggregation", required = true) Integer multiplier,
            @Parameter(description = "Time span for aggregation (e.g., day, minute)", required = true) String timespan,
            @Parameter(description = "Start date in YYYY-MM-DD format", required = true) String from,
            @Parameter(description = "End date in YYYY-MM-DD format", required = true) String to) {
        String url = String.format(
                "https://api.polygon.io/v2/aggs/ticker/%s/range/%d/%s/%s/%s?adjusted=true&sort=asc&limit=50000&apiKey=%s",
                ticker, multiplier, timespan, from, to, polygonApiKey);
        return restTemplate.getForEntity(url, String.class);
    }
}
