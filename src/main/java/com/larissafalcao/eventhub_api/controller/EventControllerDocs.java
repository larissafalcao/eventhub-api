package com.larissafalcao.eventhub_api.controller;

import com.larissafalcao.eventhub_api.dto.request.CreateEventRequest;
import com.larissafalcao.eventhub_api.dto.request.UpdateEventRequest;
import com.larissafalcao.eventhub_api.dto.response.EventResponse;
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

@Tag(name = "Events", description = "Endpoints for event management")
public interface EventControllerDocs {

    @Operation(summary = "Create event", description = "Creates a new event")
    @ApiResponse(responseCode = "201", description = "Event created successfully",
            content = @Content(schema = @Schema(implementation = EventResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<EventResponse> create(CreateEventRequest request);

    @Operation(summary = "List events", description = "Returns all events")
    @ApiResponse(responseCode = "200", description = "Events returned successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = EventResponse.class))))
    ResponseEntity<List<EventResponse>> list();

    @Operation(summary = "Get event by id", description = "Returns event details by id")
    @ApiResponse(responseCode = "200", description = "Event found",
            content = @Content(schema = @Schema(implementation = EventResponse.class)))
    @ApiResponse(responseCode = "404", description = "Event not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<EventResponse> getById(@Parameter(description = "Event id", example = "1") Long id);

    @Operation(summary = "Update event", description = "Updates an existing event by id")
    @ApiResponse(responseCode = "200", description = "Event updated successfully",
            content = @Content(schema = @Schema(implementation = EventResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Event not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<EventResponse> update(
            @Parameter(description = "Event id", example = "1") Long id,
            UpdateEventRequest request);

    @Operation(summary = "Delete event", description = "Deletes an event by id")
    @ApiResponse(responseCode = "204", description = "Event deleted successfully")
    @ApiResponse(responseCode = "404", description = "Event not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<Void> delete(@Parameter(description = "Event id", example = "1") Long id);
}
