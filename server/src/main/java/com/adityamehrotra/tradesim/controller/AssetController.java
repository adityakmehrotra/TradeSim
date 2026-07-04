package com.adityamehrotra.tradesim.controller;

import com.adityamehrotra.tradesim.dto.AssetRequest;
import com.adityamehrotra.tradesim.repository.AssetRepository;
import com.adityamehrotra.tradesim.service.AssetService;
import org.springframework.http.HttpStatus;
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
