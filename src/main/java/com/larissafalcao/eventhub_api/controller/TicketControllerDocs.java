package com.larissafalcao.eventhub_api.controller;

import com.larissafalcao.eventhub_api.dto.request.PurchaseTicketRequest;
import com.larissafalcao.eventhub_api.dto.response.TicketResponse;
import com.larissafalcao.eventhub_api.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Tickets", description = "Endpoints for ticket purchase and listing")
public interface TicketControllerDocs {

    @Operation(summary = "Purchase ticket", description = "Purchases a ticket for an event")
    @ApiResponse(responseCode = "201", description = "Ticket purchased successfully",
            content = @Content(schema = @Schema(implementation = TicketResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Event or participant not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Event capacity reached",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<TicketResponse> purchaseTicket(
            @Parameter(description = "Event id", example = "1") Long eventId,
            PurchaseTicketRequest request);

    @Operation(summary = "List participant tickets", description = "Returns all tickets from a participant")
    @ApiResponse(responseCode = "200", description = "Tickets returned successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TicketResponse.class))))
    @ApiResponse(responseCode = "404", description = "Participant not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<List<TicketResponse>> listTicketsByParticipant(
            @Parameter(description = "Participant id", example = "1") Long participantId);
}
