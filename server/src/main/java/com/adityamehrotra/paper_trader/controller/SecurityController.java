package com.adityamehrotra.paper_trader.controller;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.adityamehrotra.paper_trader.model.SecurityModel;
import com.adityamehrotra.paper_trader.service.SecurityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@CrossOrigin
@RequestMapping("/paper_trader/security")
@Validated
@Tag(name = "Security Management", description = "Endpoints for managing securities, including creation, deletion, and retrieval.")
public class SecurityController {

  private final SecurityService securityService;

  public SecurityController(SecurityService securityService) {
    this.securityService = securityService;
  }

  @Operation(summary = "Create a Security", description = "Create a single security in the system.")
  @ApiResponses({
          @ApiResponse(responseCode = "201", description = "Security created successfully"),
          @ApiResponse(responseCode = "400", description = "Invalid input provided"),
          @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("/create/one")
  public ResponseEntity<SecurityModel> createOne(
          @Parameter(description = "SecurityModel object to be created", required = true)
          @Valid @RequestBody SecurityModel securityModel) {
    SecurityModel createdSecurity = securityService.createOne(securityModel);
    return new ResponseEntity<>(createdSecurity, HttpStatus.CREATED);
  }

  @Operation(summary = "Create Multiple Securities", description = "Create multiple securities in the system.")
  @ApiResponses({
          @ApiResponse(responseCode = "201", description = "Securities created successfully"),
          @ApiResponse(responseCode = "400", description = "Invalid input provided"),
          @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("/create/many")
  public ResponseEntity<List<SecurityModel>> createMany(
          @Parameter(description = "List of SecurityModel objects to be created", required = true)
          @Valid @RequestBody List<SecurityModel> securityModelList) {
                System.out.println(securityModelList);
    List<SecurityModel> createdSecurity = securityService.createMany(securityModelList);
    return new ResponseEntity<>(createdSecurity, HttpStatus.CREATED);
  }

  @Operation(summary = "Get Security Suggestions", description = "Retrieve suggestions for securities based on user input.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Suggestions retrieved successfully"),
          @ApiResponse(responseCode = "400", description = "Invalid input provided"),
          @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/suggestion/{userInput}")
  public ResponseEntity<Set<SecurityModel>> getSuggestion(
          @Parameter(description = "User input for generating security suggestions", required = true)
          @PathVariable @NotNull String userInput) {
    Set<SecurityModel> suggestions = securityService.getSuggestion(userInput);
    return new ResponseEntity<>(suggestions, HttpStatus.OK);
  }

  @Operation(summary = "Test Security Suggestions", description = "Retrieve test suggestions for securities based on user input.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Test suggestions retrieved successfully"),
          @ApiResponse(responseCode = "400", description = "Invalid input provided"),
          @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/suggestion/test/{userInput}")
  public ResponseEntity<Set<SecurityModel>> getSuggestionTest(
          @Parameter(description = "User input for generating test security suggestions", required = true)
          @PathVariable @NotNull String userInput) {
    Set<SecurityModel> testSuggestions = securityService.getSuggestionTest(userInput);
    return new ResponseEntity<>(testSuggestions, HttpStatus.OK);
  }

  @Operation(summary = "Get All Securities", description = "Retrieve a list of all securities in the system.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Securities retrieved successfully"),
          @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/all")
  public ResponseEntity<List<SecurityModel>> getAllSecurities() {
    List<SecurityModel> securities = securityService.getAllSecurities();
    return new ResponseEntity<>(securities, HttpStatus.OK);
  }

  @Operation(summary = "Delete Security by ID", description = "Delete a security by its ID.")
  @ApiResponses({
          @ApiResponse(responseCode = "204", description = "Security deleted successfully"),
          @ApiResponse(responseCode = "404", description = "Security not found"),
          @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @DeleteMapping("/delete/{id}")
  public ResponseEntity<Void> deleteById(
          @Parameter(description = "ID of the security to delete", required = true)
          @PathVariable @NotNull String id) {
    securityService.deleteById(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  // Centralized Exception Handling
  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
    return new ResponseEntity<>("Invalid request: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<String> handleGeneralException(Exception ex) {
    return new ResponseEntity<>("An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
