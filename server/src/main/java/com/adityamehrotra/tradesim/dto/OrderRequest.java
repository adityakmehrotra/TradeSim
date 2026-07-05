package com.adityamehrotra.tradesim.dto;

import lombok.Getter;

@Getter
public class OrderRequest {
  private Integer portfolioID;
  private String symbol;
  private String side;
  private String type;
  private Long limitPriceCents;
  private long quantity;
}
