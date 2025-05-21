package com.adityamehrotra.paper_trader.repository;

import com.adityamehrotra.paper_trader.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, Integer> {
    boolean existsByEmail(@Email(message = "Email address should be valid") @NotEmpty(message = "Email address cannot be empty") String email);
    boolean existsByUsername(@NotEmpty(message = "Username cannot be empty") String username);
    User findByEmail(@Email(message = "Email address should be valid") @NotEmpty(message = "Email address cannot be empty") String email);
    User findByUsername(@NotEmpty(message = "Username cannot be empty") String username);
    User findByUserID(@NotEmpty(message = "User ID cannot be empty") Integer userID);
    int getNextID();
}
