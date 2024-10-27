package com.adityamehrotra.paper_trader.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PolygonAPIService {

    @Value("${apiKey}")
    private String polygonApiKey;

    private final RestTemplate restTemplate;

    public PolygonAPIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> getCurrentPrice(String ticker) {
        String url = String.format(
                "https://api.polygon.io/v2/snapshot/locale/us/markets/stocks/tickers/%s?apiKey=%s",
                ticker, polygonApiKey);
        return restTemplate.getForEntity(url, String.class);
    }

    public ResponseEntity<String> getCurrentNews(String ticker) {
        String url = String.format(
                "https://api.polygon.io/v2/reference/news?ticker=%s&order=desc&limit=50&apiKey=%s",
                ticker, polygonApiKey);
        return restTemplate.getForEntity(url, String.class);
    }

    public ResponseEntity<String> getKeyStatistics(String ticker) {
        String url = String.format(
                "https://api.polygon.io/v2/aggs/ticker/%s/prev?adjusted=true&apiKey=%s",
                ticker, polygonApiKey);
        return restTemplate.getForEntity(url, String.class);
    }

    public ResponseEntity<String> getCompanyData(String ticker) {
        String url = String.format(
                "https://api.polygon.io/v3/reference/tickers/%s?apiKey=%s", ticker, polygonApiKey);
        return restTemplate.getForEntity(url, String.class);
    }

    public ResponseEntity<String> getChart(String ticker, Integer multiplier, String timespan, String from, String to) {
        String url = String.format(
                "https://api.polygon.io/v2/aggs/ticker/%s/range/%d/%s/%s/%s?adjusted=true&sort=asc&limit=50000&apiKey=%s",
                ticker, multiplier, timespan, from, to, polygonApiKey);
        return restTemplate.getForEntity(url, String.class);
    }
}
