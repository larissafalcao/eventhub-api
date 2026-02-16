package com.larissafalcao.eventhub_api.controller;

import com.larissafalcao.eventhub_api.dto.request.CreateParticipantRequest;
import com.larissafalcao.eventhub_api.dto.response.ParticipantResponse;
import com.larissafalcao.eventhub_api.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Participants", description = "Endpoints for participant management")
public interface ParticipantControllerDocs {

    @Operation(summary = "Create participant", description = "Creates a new participant")
    @ApiResponse(responseCode = "201", description = "Participant created successfully",
            content = @Content(schema = @Schema(implementation = ParticipantResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<ParticipantResponse> create(CreateParticipantRequest request);
}
