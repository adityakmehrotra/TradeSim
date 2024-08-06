package com.adityamehrotra.paper_trader.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("")
public class HomeController {
    @GetMapping("/")
    public String health() {
        return ("Welcome to the TradeSim Backend");
    }
}
