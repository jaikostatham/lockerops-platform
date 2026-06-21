package com.jaico.lockerops.ticket.api.controller;

import com.jaico.lockerops.shared.exception.ApiErrorResponse;
import com.jaico.lockerops.ticket.api.dto.response.TicketResponse;
import com.jaico.lockerops.ticket.application.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
@Tag(
        name = "Tickets",
        description = "Operations for retrieving reservation tickets"
)
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get ticket by id",
            description = "Returns a ticket by its internal identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ticket retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ticket not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        TicketResponse ticket = ticketService.getTicketById(id);

        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/code/{ticketCode}")
    @Operation(
            summary = "Get ticket by code",
            description = "Returns a ticket by its public ticket code."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ticket retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ticket not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<TicketResponse> getTicketByCode(
            @PathVariable String ticketCode
    ) {
        TicketResponse ticket = ticketService.getTicketByCode(ticketCode);

        return ResponseEntity.ok(ticket);
    }
}
