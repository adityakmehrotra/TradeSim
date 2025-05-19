package com.adityamehrotra.paper_trader.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "TradeSim-Asset")
public class Asset {
    @Id
    @NotNull(message = "Asset ID cannot be null")
    @Min(value = 1, message = "Asset ID must be greater than 0")
    private Integer assetID;
    
    @NotEmpty(message = "Ticker cannot be empty")
    private String ticker;

    @NotEmpty(message = "Name cannot be empty")
    private String name;

    @NotEmpty(message = "Type cannot be empty")
    private String type;

    private String isin;
}
