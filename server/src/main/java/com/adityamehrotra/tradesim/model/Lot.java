package com.adityamehrotra.tradesim.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One purchase lot in a FIFO position: a quantity of shares bought at a given price in cents. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Lot {
  private long quantity;
  private long priceCents;
}
