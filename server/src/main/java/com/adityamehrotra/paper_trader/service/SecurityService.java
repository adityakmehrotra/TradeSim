package com.adityamehrotra.paper_trader.service;

import com.adityamehrotra.paper_trader.model.SecurityModel;
import com.adityamehrotra.paper_trader.repository.SecurityRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service class for managing and retrieving security-related information.
 */
@Service
public class SecurityService {

    private final SecurityRepository securityRepository;

    /**
     * Constructor for SecurityService.
     *
     * @param securityRepository Repository for managing SecurityModel entities.
     */
    public SecurityService(SecurityRepository securityRepository) {
        this.securityRepository = securityRepository;
    }

    /**
     * Creates and saves a single security.
     *
     * @param securityModel The SecurityModel object to save.
     * @return The saved SecurityModel object.
     */
    @Operation(
            summary = "Create a single security",
            description = "Saves a new security to the database."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Security created successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid input."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public SecurityModel createOne(
            @Parameter(description = "SecurityModel object to save", required = true) SecurityModel securityModel) {
        return securityRepository.save(securityModel);
    }

    /**
     * Creates and saves multiple securities.
     *
     * @param securityModelList A list of SecurityModel objects to save.
     */
    @Operation(
            summary = "Create multiple securities",
            description = "Saves multiple new securities to the database."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Securities created successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid input."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public void createMany(
            @Parameter(description = "List of SecurityModel objects to save", required = true) List<SecurityModel> securityModelList) {
        securityRepository.saveAll(securityModelList);
    }

    /**
     * Retrieves a set of securities whose code or name starts with the given user input.
     *
     * @param userInput The input string to search for.
     * @return A set of matching SecurityModel objects.
     */
    @Operation(
            summary = "Get security suggestions",
            description = "Fetches securities whose code or name starts with the given input."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suggestions fetched successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid input."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public Set<SecurityModel> getSuggestion(
            @Parameter(description = "Input string to search for", required = true) String userInput) {
        userInput = userInput.toLowerCase();
        Set<SecurityModel> codeList = new HashSet<>();

        for (SecurityModel securityModel : securityRepository.findAll()) {
            if (securityModel.getCode().toLowerCase().startsWith(userInput)) {
                codeList.add(securityModel);
            } else if (securityModel.getName().toLowerCase().startsWith(userInput)) {
                codeList.add(securityRepository.findById(securityModel.getCode()).get());
            }
        }
        return codeList;
    }

    /**
     * Retrieves a set of securities starting with the given input using a custom repository method.
     *
     * @param userInput The input string to search for.
     * @return A set of matching SecurityModel objects.
     */
    @Operation(
            summary = "Get security suggestions (test method)",
            description = "Fetches securities whose code or name starts with the given input using a test repository method."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suggestions fetched successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid input."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public Set<SecurityModel> getSuggestionTest(
            @Parameter(description = "Input string to search for", required = true) String userInput) {
        userInput = userInput.toLowerCase();
        return securityRepository.findSecuritiesStartingWith(userInput);
    }

    /**
     * Retrieves all securities from the database.
     *
     * @return A list of all SecurityModel objects.
     */
    @Operation(
            summary = "Get all securities",
            description = "Fetches all securities from the database."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Securities fetched successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public List<SecurityModel> getAllSecurities() {
        return securityRepository.findAll();
    }

    /**
     * Deletes a security by its ID.
     *
     * @param id The ID of the security to delete.
     */
    @Operation(
            summary = "Delete a security by ID",
            description = "Deletes the security with the specified ID from the database."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Security deleted successfully."),
            @ApiResponse(responseCode = "404", description = "Security not found."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public void deleteById(
            @Parameter(description = "ID of the security to delete", required = true) String id) {
        securityRepository.deleteById(id);
    }
}
