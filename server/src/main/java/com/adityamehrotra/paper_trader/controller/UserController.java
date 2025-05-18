package com.adityamehrotra.paper_trader.controller;

import com.adityamehrotra.paper_trader.dto.LoginRequest;
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
            int userID = userService.registerUser(user);
            Map<String, Object> response = new HashMap<>();
            response.put("token", "INSERT TOKEN HERE");
            response.put("id", userID);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> login(@RequestBody LoginRequest user) {
        try {
            int userID = userService.authenticateUser(user);
            Map<String, Object> response = new HashMap<>();
            response.put("token", "INSERT TOKEN HERE");
            response.put("id", userID);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/firstName")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getFirstName(@RequestParam Integer userID) {
        try {
            String firstName = userService.getUser(userID).getFirstName();
            Map<String, String> response = new HashMap<>();
            response.put("firstName", firstName);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/firstName")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> updateFirstName(@RequestParam Integer userID, @RequestParam String firstName) {
        try {
            userService.updateFirstName(userID, firstName);
            Map<String, String> response = new HashMap<>();
            response.put("message", "First name updated successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/lastName")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getLastName(@RequestParam Integer userID) {
        try {
            String lastName = userService.getUser(userID).getLastName();
            Map<String, String> response = new HashMap<>();
            response.put("lastName", lastName);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/lastName")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> updateLastName(@RequestParam Integer userID, @RequestParam String lastName) {
        try {
            userService.updateLastName(userID, lastName);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Last name updated successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/username")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getUsername(@RequestParam Integer userID) {
        try {
            String username = userService.getUser(userID).getUsername();
            Map<String, String> response = new HashMap<>();
            response.put("username", username);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/username")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> updateUsername(@RequestParam Integer userID, @RequestParam String username) {
        try {
            userService.updateUsername(userID, username);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Username updated successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/password")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getPassword(@RequestParam Integer userID) {
        try {
            String password = userService.getUser(userID).getPassword();
            Map<String, String> response = new HashMap<>();
            response.put("password", password);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> updatePassword(@RequestParam Integer userID, @RequestParam String password) {
        try {
            userService.updatePassword(userID, password);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password updated successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/email")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getEmail(@RequestParam Integer userID) {
        try {
            String email = userService.getUser(userID).getEmail();
            Map<String, String> response = new HashMap<>();
            response.put("email", email);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/email")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> updateEmail(@RequestParam Integer userID, @RequestParam String email) {
        try {
            userService.updateEmail(userID, email);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email updated successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteUser(@RequestParam Integer userID) {
        try {
            userService.deleteUser(userID);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/portfolio")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getPortfolio(@RequestParam Integer userID) {
        try {
            User user = userService.getUser(userID);
            Map<String, Object> response = new HashMap<>();
            response.put("portfolio", user.getPortfolioList());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/portfolio")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> addPortfolio(@RequestParam Integer userID, @RequestParam Integer portfolioID) {
        try {
            userService.addPortfolio(userID, portfolioID);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Portfolio added successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/portfolio")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> removePortfolio(@RequestParam Integer userID, @RequestParam Integer portfolioID) {
        try {
            userService.removePortfolio(userID, portfolioID);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Portfolio removed successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
}
