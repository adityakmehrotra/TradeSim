package com.adityamehrotra.paper_trader.controller;

import com.adityamehrotra.paper_trader.dto.HoldingRequest;
import com.adityamehrotra.paper_trader.dto.UpdateHoldingRequest;
import com.adityamehrotra.paper_trader.model.Holding;
import com.adityamehrotra.paper_trader.repository.HoldingRepository;
import com.adityamehrotra.paper_trader.service.HoldingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/tradesim/api/holding")
@Validated
public class HoldingController {
    private final HoldingService holdingService;
    private final HoldingRepository holdingRepository;

    public HoldingController(HoldingService holdingService, HoldingRepository holdingRepository) {
        this.holdingService = holdingService;
        this.holdingRepository = holdingRepository;
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> addHolding(@RequestBody HoldingRequest holding) {
        try {
            if (holdingService.existsByHoldingID(holding.getHoldingID())) {
                UpdateHoldingRequest updateHoldingRequest = new UpdateHoldingRequest(
                        holding.getHoldingID(),
                        holding.getTransactionID(),
                        holding.getShares(),
                        holding.getPrice(),
                        "buy"
                );
                holdingService.updateHolding(updateHoldingRequest);
            } else {
                holdingService.createHolding(holding);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", holding.getHoldingID());

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> buyHolding(@RequestBody UpdateHoldingRequest holding) {
        try {
            if (holding.getAction().equalsIgnoreCase("buy") || holding.getAction().equalsIgnoreCase("sell")) {
                holdingService.updateHolding(holding);
            } else {
                throw new IllegalArgumentException("Invalid action. Use 'buy' or 'sell'.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Holding updated with ID: " + holding.getHoldingID());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/shares")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getHolding(@RequestParam int holdingID) {
        try {
            Holding holding = holdingService.getHolding(holdingID);
            Map<String, Double> response = new HashMap<>();
            response.put("holdingID", (double) holding.getHoldingID());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/avgValue")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getAvgValue(@RequestParam int holdingID) {
        try {
            Holding holding = holdingService.getHolding(holdingID);
            Map<String, Double> response = new HashMap<>();
            response.put("avgValue", holding.getAvgValue());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/active")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getActive(@RequestParam int holdingID) {
        try {
            Holding holding = holdingService.getHolding(holdingID);
            Map<String, Boolean> response = new HashMap<>();
            response.put("active", holding.getActive());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
}
