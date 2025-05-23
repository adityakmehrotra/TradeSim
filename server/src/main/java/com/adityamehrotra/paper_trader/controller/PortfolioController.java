package com.adityamehrotra.paper_trader.controller;

import com.adityamehrotra.paper_trader.dto.PortfolioRequest;
import com.adityamehrotra.paper_trader.model.Portfolio;
import com.adityamehrotra.paper_trader.repository.PortfolioRepository;
import com.adityamehrotra.paper_trader.service.PortfolioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/tradesim/api/portfolio")
@Validated
public class PortfolioController {
    private final PortfolioService portfolioService;
    private final PortfolioRepository portfolioRepository;
    private final UserController userController;

    public PortfolioController(PortfolioService portfolioService, PortfolioRepository portfolioRepository, UserController userController) {
        this.portfolioService = portfolioService;
        this.portfolioRepository = portfolioRepository;
        this.userController = userController;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createPortfolio(@RequestBody PortfolioRequest portfolio) {
        try {
            int portfolioID = portfolioService.createPortfolio(portfolio);
            userController.addPortfolio(portfolio.getAccountID(), portfolioID);
            Map<String, Object> response = new HashMap<>();
            response.put("token", "INSERT TOKEN HERE");
            response.put("id", portfolioID);


            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/name")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getPortfolioName(@RequestParam Integer portfolioID) {
        try {
            String portfolioName = portfolioService.getPortfolio(portfolioID).getName();
            Map<String, String> response = new HashMap<>();
            response.put("name", portfolioName);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/name")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> updatePortfolioName(@RequestParam Integer portfolioID, @RequestParam String name) {
        try {
            portfolioService.updatePortfolioName(portfolioID, name);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Portfolio name updated successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/description")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getPortfolioDescription(@RequestParam Integer portfolioID) {
        try {
            String portfolioDescription = portfolioService.getPortfolio(portfolioID).getDescription();
            Map<String, String> response = new HashMap<>();
            response.put("description", portfolioDescription);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/description")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> updatePortfolioDescription(@RequestParam Integer portfolioID, @RequestParam String description) {
        try {
            portfolioService.updatePortfolioDescription(portfolioID, description);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Portfolio description updated successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/cash")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getPortfolioCash(@RequestParam Integer portfolioID) {
        try {
            Double portfolioCash = portfolioService.getPortfolio(portfolioID).getCash();
            Map<String, Double> response = new HashMap<>();
            response.put("cash", portfolioCash);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/cash")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> updatePortfolioCash(@RequestParam Integer portfolioID, @RequestParam Double cash) {
        try {
            portfolioService.updatePortfolioCash(portfolioID, cash);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Portfolio cash updated successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/initialBalance")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getPortfolioInitialBalance(@RequestParam Integer portfolioID) {
        try {
            Double initialBalance = portfolioService.getPortfolio(portfolioID).getInitialBalance();
            Map<String, Double> response = new HashMap<>();
            response.put("initialBalance", initialBalance);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/transactionList")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getPortfolioTransactionList(@RequestParam Integer portfolioID) {
        try {
            Portfolio portfolio = portfolioService.getPortfolio(portfolioID);
            Map<String, Object> response = new HashMap<>();
            response.put("transactionList", portfolio.getTransactionList());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/transactionList")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> updatePortfolioTransactionList(@RequestParam Integer portfolioID, @RequestParam Integer transactionID) {
        try {
            portfolioService.addTransaction(portfolioID, transactionID);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Portfolio transaction list updated successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/transactionList")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deletePortfolioTransaction(@RequestParam Integer portfolioID, @RequestParam Integer transactionID) {
        try {
            portfolioService.removeTransaction(portfolioID, transactionID);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Transaction deleted successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/holdingsList")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getPortfolioHoldingsList(@RequestParam Integer portfolioID) {
        try {
            Portfolio portfolio = portfolioService.getPortfolio(portfolioID);
            Map<String, Object> response = new HashMap<>();
            response.put("holdingsList", portfolio.getHoldingsList());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/holdingsList")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> updatePortfolioHoldingsList(@RequestParam Integer portfolioID, @RequestParam Integer holdingID) {
        try {
            portfolioService.addHolding(portfolioID, holdingID);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Portfolio holdings list updated successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/holdingsList")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deletePortfolioHolding(@RequestParam Integer portfolioID, @RequestParam Integer holdingID) {
        try {
            portfolioService.removeHolding(portfolioID, holdingID);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Holding deleted successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deletePortfolio(@RequestParam Integer portfolioID) {
        try {
            portfolioService.deletePortfolio(portfolioID);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Portfolio deleted successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
}
