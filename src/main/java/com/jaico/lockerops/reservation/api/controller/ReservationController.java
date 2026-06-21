package com.jaico.lockerops.reservation.api.controller;

import com.jaico.lockerops.reservation.api.dto.request.CreateReservationRequest;
import com.jaico.lockerops.reservation.api.dto.response.ReservationResponse;
import com.jaico.lockerops.shared.exception.ApiErrorResponse;
import com.jaico.lockerops.reservation.application.service.ReservationService;
import com.jaico.lockerops.ticket.api.dto.response.ReservationTicketResponse;
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
@RequestMapping("/api/reservations")
@Tag(
        name = "Reservations",
        description = "Operations for managing locker reservations"
)
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @Operation(
            summary = "Create reservation",
            description = "Creates a new reservation for an available locker compartment."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Reservation created successfully"
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
                    description = "Locker compartment is not available for reservation",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<ReservationTicketResponse> createReservation(
            @Valid @RequestBody CreateReservationRequest request
    ) {
        ReservationTicketResponse createdReservation =
                reservationService.createReservation(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdReservation);
    }

    @GetMapping
    @Operation(
            summary = "Get all reservations",
            description = "Returns all reservations registered in the platform."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservations retrieved successfully"
            )
    })
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        List<ReservationResponse> reservations =
                reservationService.getAllReservations();

        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get reservation by id",
            description = "Returns a single reservation by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<ReservationResponse> getReservationById(
            @PathVariable Long id
    ) {
        ReservationResponse reservation =
                reservationService.getReservationById(id);

        return ResponseEntity.ok(reservation);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel reservation",
            description = "Cancels an active reservation and releases the associated locker compartment."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation cancelled successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Reservation cannot be cancelled because it is not active",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<ReservationResponse> cancelReservation(
            @PathVariable Long id
    ) {
        ReservationResponse cancelledReservation =
                reservationService.cancelReservation(id);

        return ResponseEntity.ok(cancelledReservation);
    }
}
