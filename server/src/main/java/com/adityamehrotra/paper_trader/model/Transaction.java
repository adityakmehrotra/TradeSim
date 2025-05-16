package com.adityamehrotra.paper_trader.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "TradeSim-Transaction")
public class Transaction {
    @Id
    @NotNull(message = "Transaction ID cannot be null")
    @Min(value = 1, message = "Transaction ID must be greater than 0")
    private Integer transactionID;

    @NotNull(message = "Portfolio ID cannot be null")
    @Min(value = 1, message = "Portfolio ID must be greater than 0")
    private Integer portfolioID;

    @NotNull(message = "Holding ID cannot be null")
    @Min(value = 1, message = "Holding ID must be greater than 0")
    private Integer holdingID;

    @NotEmpty(message = "Ticker cannot be empty")
    private String ticker;

    @NotNull(message = "Filled date cannot be null")
    private Instant filledDate;  

    @NotNull(message = "Submitted date cannot be null")
    private Instant submittedDate;  

    @NotEmpty(message = "Type cannot be empty")
    @Size(max = 20, message = "Type must not exceed 20 characters")
    private String type;

    @NotEmpty(message = "Status cannot be empty")
    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    @NotNull(message = "Shares cannot be null")
    @Min(value = 0, message = "Shares must be non-negative")
    private Double shares;

    @NotNull(message = "Price cannot be null")
    @Min(value = 0, message = "Price must be non-negative")
    private Double price;

    @NotNull(message = "Total amount cannot be null")
    @Min(value = 0, message = "Total amount must be non-negative")
    private Double totalAmount;
}
