package com.jaico.lockerops.compartment.api.controller;

import com.jaico.lockerops.compartment.api.dto.request.CreateLockerCompartmentRequest;
import com.jaico.lockerops.compartment.api.dto.response.LockerCompartmentResponse;
import com.jaico.lockerops.compartment.api.dto.request.UpdateLockerCompartmentRequest;
import com.jaico.lockerops.shared.exception.ApiErrorResponse;
import com.jaico.lockerops.compartment.application.service.LockerCompartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(
        name = "Locker Compartments",
        description = "Operations for managing locker compartments"
)
public class LockerCompartmentController {

    private final LockerCompartmentService lockerCompartmentService;

    public LockerCompartmentController(LockerCompartmentService lockerCompartmentService) {
        this.lockerCompartmentService = lockerCompartmentService;
    }

    @PostMapping("/locker-stations/{lockerStationId}/compartments")
    @Operation(
            summary = "Create locker compartment",
            description = "Creates a new locker compartment inside an existing locker station."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Locker compartment created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Locker station not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Locker compartment already exists in this station",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<LockerCompartmentResponse> createLockerCompartment(
            @PathVariable Long lockerStationId,
            @Valid @RequestBody CreateLockerCompartmentRequest request
    ) {
        LockerCompartmentResponse createdLockerCompartment =
                lockerCompartmentService.createLockerCompartment(
                        lockerStationId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdLockerCompartment);
    }

    @GetMapping("/locker-stations/{lockerStationId}/compartments")
    @Operation(
            summary = "Get locker compartments by station id",
            description = "Returns all locker compartments that belong to a specific locker station."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Locker compartments retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Locker station not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<List<LockerCompartmentResponse>> getLockerCompartmentsByStationId(
            @PathVariable Long lockerStationId
    ) {
        List<LockerCompartmentResponse> lockerCompartments =
                lockerCompartmentService.getLockerCompartmentsByStationId(lockerStationId);

        return ResponseEntity.ok(lockerCompartments);
    }

    @GetMapping("/locker-compartments/{id}")
    @Operation(
            summary = "Get locker compartment by id",
            description = "Returns a single locker compartment by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Locker compartment retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Locker compartment not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<LockerCompartmentResponse> getLockerCompartmentById(
            @PathVariable Long id
    ) {
        LockerCompartmentResponse lockerCompartment =
                lockerCompartmentService.getLockerCompartmentById(id);

        return ResponseEntity.ok(lockerCompartment);
    }

    @PutMapping("/locker-compartments/{id}")
    @Operation(
            summary = "Update locker compartment",
            description = "Updates an existing locker compartment by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Locker compartment updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Locker compartment not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Locker compartment number already exists in this station",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<LockerCompartmentResponse> updateLockerCompartment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLockerCompartmentRequest request
    ) {
        LockerCompartmentResponse updatedLockerCompartment =
                lockerCompartmentService.updateLockerCompartment(
                        id,
                        request
                );

        return ResponseEntity.ok(updatedLockerCompartment);
    }

    @DeleteMapping("/locker-compartments/{id}")
    @Operation(
            summary = "Delete locker compartment",
            description = "Deletes an existing locker compartment by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Locker compartment deleted successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Locker compartment not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<Void> deleteLockerCompartment(@PathVariable Long id) {
        lockerCompartmentService.deleteLockerCompartment(id);

        return ResponseEntity.noContent().build();
    }
}