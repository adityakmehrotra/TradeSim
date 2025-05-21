package com.adityamehrotra.paper_trader.dto;

import lombok.Getter;
import lombok.Setter;

@Getter

public class AssetRequest {
    @Setter
    private String ticker;
    @Setter
    private String name;

    private String type;
    private String isin;

    public AssetRequest(String ticker, String name, String type, String isin) {
        this.ticker = ticker;
        this.name = name;
        this.type = type;
        this.isin = isin;
    }
}
