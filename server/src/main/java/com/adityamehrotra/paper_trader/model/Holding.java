package com.adityamehrotra.paper_trader.model;

import java.util.List;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Document(collection = "TradeSim-Holding")
public class Holding {
    @Id
    @NotNull(message = "Holding ID cannot be null")
    @Min(value = 1, message = "Holding ID must be greater than 0")
    private Integer holdingID;

    @NotNull(message = "Portfolio ID cannot be null")
    @Min(value = 1, message = "Portfolio ID must be greater than 0")
    private Integer portfolioID;

    private List<
            @NotNull(message = "Transaction ID cannot be null")
            @Min(value = 1, message = "Transaction ID must be greater than 0")
            Integer> transactionList;

    private Double shares;

    private Double avgValue;

    private boolean active;

    public boolean getActive() {
        return active;
    }
}
