package com.jaico.lockerops.station;

import com.jaico.lockerops.station.CreateLockerStationRequest;
import com.jaico.lockerops.station.LockerStationResponse;
import com.jaico.lockerops.station.UpdateLockerStationRequest;
import com.jaico.lockerops.shared.exception.ApiErrorResponse;
import com.jaico.lockerops.station.LockerStationService;
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
@RequestMapping("/api/locker-stations")
@Tag(
        name = "Locker Stations",
        description = "Operations for managing locker stations"
)
public class LockerStationController {

    private final LockerStationService lockerStationService;

    public LockerStationController(LockerStationService lockerStationService) {
        this.lockerStationService = lockerStationService;
    }

    @GetMapping
    @Operation(
            summary = "Get all locker stations",
            description = "Returns the full list of locker stations registered in the platform."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Locker stations retrieved successfully"
    )
    public ResponseEntity<List<LockerStationResponse>> getAllLockerStations() {
        List<LockerStationResponse> lockerStations = lockerStationService.getAllLockerStations();

        return ResponseEntity.ok(lockerStations);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get locker station by id",
            description = "Returns a single locker station by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Locker station retrieved successfully"
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
    public ResponseEntity<LockerStationResponse> getLockerStationById(@PathVariable Long id) {
        LockerStationResponse lockerStation = lockerStationService.getLockerStationById(id);

        return ResponseEntity.ok(lockerStation);
    }

    @PostMapping
    @Operation(
            summary = "Create locker station",
            description = "Creates a new locker station using the provided request payload."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Locker station created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<LockerStationResponse> createLockerStation(
            @Valid @RequestBody CreateLockerStationRequest request) {

        LockerStationResponse createdLockerStation = lockerStationService.createLockerStation(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdLockerStation);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update locker station",
            description = "Updates an existing locker station by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Locker station updated successfully"
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
            )
    })
    public ResponseEntity<LockerStationResponse> updateLockerStation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLockerStationRequest request) {

        LockerStationResponse updatedLockerStation = lockerStationService.updateLockerStation(
                id,
                request
        );

        return ResponseEntity.ok(updatedLockerStation);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete locker station",
            description = "Deletes an existing locker station by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Locker station deleted successfully",
                    content = @Content
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
    public ResponseEntity<Void> deleteLockerStation(@PathVariable Long id) {
        lockerStationService.deleteLockerStation(id);

        return ResponseEntity.noContent().build();
    }
}