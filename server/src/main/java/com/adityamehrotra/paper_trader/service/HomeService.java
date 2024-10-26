package com.adityamehrotra.paper_trader.service;

import org.springframework.stereotype.Service;

@Service
public class HomeService {
    public String health() {
        return ("Welcome to the TradeSim Backend");
    }
}
