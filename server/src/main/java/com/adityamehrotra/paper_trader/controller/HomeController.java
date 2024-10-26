package com.adityamehrotra.paper_trader.controller;

import com.adityamehrotra.paper_trader.service.HomeService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("")
public class HomeController {
    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping("/")
    public String health() {
        return(homeService.health());
    }
}
