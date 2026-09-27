package com.jaico.lockerops.ticket.api.controller;

import com.jaico.lockerops.shared.exception.ApiErrorResponse;
import com.jaico.lockerops.ticket.api.dto.request.ValidateAccessCodeRequest;
import com.jaico.lockerops.ticket.api.dto.response.AccessValidationResponse;
import com.jaico.lockerops.ticket.application.service.AccessCodeService;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
@RequestMapping("/api/access-codes")
@Tag(
        name = "Access Codes",
        description = "Operations for validating locker access codes"
)
public class AccessCodeController {

    private final AccessCodeService accessCodeService;

    public AccessCodeController(AccessCodeService accessCodeService) {
        this.accessCodeService = accessCodeService;
    }

    @PostMapping("/validate")
    @Operation(
            summary = "Validate access code",
            description = "Validates a reusable access code for kiosk access simulation."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Access code validated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AccessValidationResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "granted": true,
                                              "ticketCode": "TCK-8F3K2Q9Z",
                                              "reservationId": 1,
                                              "lockerCompartmentId": 10,
                                              "compartmentNumber": 5,
                                              "reservedUntil": "2026-06-21T17:00:00Z",
                                              "validatedAt": "2026-06-21T16:15:00Z"
                                            }
                                            """
                            )
                    )
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
                    description = "Ticket or access code not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Access code cannot be used",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<AccessValidationResponse> validateAccessCode(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Ticket code and access code credentials to validate.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidateAccessCodeRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "ticketCode": "TCK-8F3K2Q9Z",
                                              "accessCode": "7K29QX4B"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody ValidateAccessCodeRequest request
    ) {
        AccessValidationResponse response =
                accessCodeService.validateAccessCode(
                        request.getTicketCode(),
                        request.getAccessCode()
                );

        return ResponseEntity.ok(response);
    }
}
