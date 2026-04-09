package com.larissafalcao.eventhub_api.controller;

import com.larissafalcao.eventhub_api.dto.request.LoginRequest;
import com.larissafalcao.eventhub_api.dto.request.RegisterRequest;
import com.larissafalcao.eventhub_api.dto.response.AuthResponse;
import com.larissafalcao.eventhub_api.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public interface AuthControllerDocs {

    @Operation(summary = "Register participant", description = "Creates a new participant user and returns a JWT token")
    @ApiResponse(responseCode = "201", description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Email already registered",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<AuthResponse> register(RegisterRequest request);

    @Operation(summary = "Register admin", description = "Creates a new admin user (requires ADMIN role)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Admin registered successfully",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Admin role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Email already registered",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<AuthResponse> registerAdmin(RegisterRequest request);

    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT token")
    @ApiResponse(responseCode = "200", description = "User authenticated successfully",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<AuthResponse> login(LoginRequest request);
}
