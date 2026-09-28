package com.jaico.lockerops.payment.api.controller;

import com.jaico.lockerops.payment.api.dto.request.SimulatePaymentRequest;
import com.jaico.lockerops.payment.api.dto.response.SimulatedPaymentResponse;
import com.jaico.lockerops.payment.application.service.SimulatedPaymentService;
import com.jaico.lockerops.shared.exception.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Simulated payment operations for the portfolio flow")
public class SimulatedPaymentController {

    private final SimulatedPaymentService simulatedPaymentService;

    public SimulatedPaymentController(SimulatedPaymentService simulatedPaymentService) {
        this.simulatedPaymentService = simulatedPaymentService;
    }

    @PostMapping("/simulate")
    @Operation(
            summary = "Simulate a payment",
            description = "Approves or declines a simulated payment for a pending reservation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment simulation completed"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Reservation cannot be paid",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<SimulatedPaymentResponse> simulatePayment(
            @Valid @RequestBody SimulatePaymentRequest request
    ) {
        return ResponseEntity.ok(simulatedPaymentService.simulatePayment(request));
    }
}
