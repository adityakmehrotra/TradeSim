package com.adityamehrotra.paper_trader.service;

import com.adityamehrotra.paper_trader.dto.RegistrationRequest;
import com.adityamehrotra.paper_trader.model.User;
import com.adityamehrotra.paper_trader.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void registerUser(RegistrationRequest user) {
        if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }

        if (user.getLastName() == null || user.getLastName().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }

        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (user.getDateCreated() == null || user.getDateCreated().toString().isEmpty()) {
            throw new IllegalArgumentException("Date created cannot be empty");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }



        int userID = userRepository.getNextID();
        System.out.println(userID);

        List<Integer> portfolioList = new ArrayList<>();

        User newUser = new User(
                userID,
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getDateCreated(),
                portfolioList
        );

        userRepository.save(newUser);
    }
}
