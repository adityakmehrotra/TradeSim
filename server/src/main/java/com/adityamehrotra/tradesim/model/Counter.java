package com.adityamehrotra.tradesim.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** One row per id sequence. The name is the document id, so a sequence cannot be duplicated. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "TradeSim-Counter")
public class Counter {
  @Id private String name;

  private int seq;
}
