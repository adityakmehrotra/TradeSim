package com.adityamehrotra.paper_trader.controller;

import com.adityamehrotra.paper_trader.dto.RegistrationRequest;
import com.adityamehrotra.paper_trader.model.User;
import com.adityamehrotra.paper_trader.repository.UserRepository;
import com.adityamehrotra.paper_trader.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/tradesim/api/user")
@Validated
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> signUp(@RequestBody RegistrationRequest user) {
        try {
            userService.registerUser(user);
            Map<String, Object> response = new HashMap<>();
            response.put("token", "INSERT TOKEN HERE");

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
}
