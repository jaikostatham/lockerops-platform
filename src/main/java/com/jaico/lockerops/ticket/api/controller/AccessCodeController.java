package com.jaico.lockerops.ticket.api.controller;

import com.jaico.lockerops.shared.exception.ApiErrorResponse;
import com.jaico.lockerops.ticket.api.dto.request.ValidateAccessCodeRequest;
import com.jaico.lockerops.ticket.api.dto.response.AccessValidationResponse;
import com.jaico.lockerops.ticket.application.service.AccessCodeService;
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
                    description = "Access code validated successfully"
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
                    description = "Access code not found",
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
            @Valid @RequestBody ValidateAccessCodeRequest request
    ) {
        AccessValidationResponse response =
                accessCodeService.validateAccessCode(request.getCode());

        return ResponseEntity.ok(response);
    }
}
