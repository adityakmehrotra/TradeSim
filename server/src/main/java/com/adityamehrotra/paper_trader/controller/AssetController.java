package com.adityamehrotra.paper_trader.controller;

import com.adityamehrotra.paper_trader.dto.AssetRequest;
import com.adityamehrotra.paper_trader.model.Asset;
import com.adityamehrotra.paper_trader.repository.AssetRepository;
import com.adityamehrotra.paper_trader.service.AssetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin
@RequestMapping("/tradesim/api/asset")
@Validated
public class AssetController {
    private final AssetService assetService;
    private final AssetRepository assetRepository;

    public AssetController(AssetService assetService, AssetRepository assetRepository) {
        this.assetService = assetService;
        this.assetRepository = assetRepository;
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public void addAsset(@RequestBody AssetRequest asset) {
        try {
            assetService.addAsset(asset);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/add/all")
    @ResponseStatus(HttpStatus.CREATED)
    public void addAllAssets(@RequestBody AssetRequest[] assets) {
        for (AssetRequest asset : assets) {
            try {
                assetService.addAsset(asset);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }
    }
}
