package com.adityamehrotra.paper_trader.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document(collection = "TradeSim-User")
public class User {
    @Id
    @NotNull(message = "User ID cannot be null")
    private Integer userID;

    @NotEmpty(message = "First name cannot be empty")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @NotEmpty(message = "Last name cannot be empty")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @NotEmpty(message = "Username cannot be empty")
    @Size(max = 50, message = "Username cannot exceed 50 characters")
    private String username;

    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    private String password;

    @Email(message = "Email address should be valid")
    @NotEmpty(message = "Email address cannot be empty")
    @Indexed(unique = true) // Ensures email addresses are unique in the database
    private String email;

    @NotNull(message = "Date created cannot be null")
    private Date dateCreated;

    @NotNull(message = "Portfolio list cannot be null")
    private List<Integer> portfolioList;
    
}
