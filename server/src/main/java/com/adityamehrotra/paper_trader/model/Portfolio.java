package com.adityamehrotra.paper_trader.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document(collection = "TradeSim-Portfolio")
public class Portfolio {
    @Id
    @NotNull(message = "Portfolio ID cannot be null")
    @Min(value = 1, message = "Portfolio ID must be greater than 0")
    private Integer portfolioID;

    @NotNull(message = "Account ID cannot be null")
    @Min(value = 1, message = "Account ID must be greater than 0")
    private Integer accountID;

    @NotEmpty(message = "Portfolio name cannot be empty")
    @Size(max = 100, message = "Portfolio name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Cash amount cannot be null")
    @Min(value = 0, message = "Cash amount must be non-negative")
    private Double cash;

    @NotNull(message = "Initial balance cannot be null")
    @Min(value = 1, message = "Initial balance must be greater than 0")
    private Double initialBalance;

    @NotNull(message = "Transaction list cannot be null")
    private List<
            @NotNull(message = "Transaction ID cannot be null")
            @Min(value = 1, message = "Transaction ID must be greater than 0")
            Integer> transactionList;

    @NotNull(message = "Holdings list cannot be null")
    private List<
            @NotNull(message = "Holding ID cannot be null")
            @Min(value = 1, message = "Holding ID must be greater than 0")
            Integer> holdingsList;
}
