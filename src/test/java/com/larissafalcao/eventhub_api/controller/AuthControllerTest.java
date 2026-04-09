package com.larissafalcao.eventhub_api.controller;

import com.larissafalcao.eventhub_api.dto.request.LoginRequest;
import com.larissafalcao.eventhub_api.dto.request.RegisterRequest;
import com.larissafalcao.eventhub_api.dto.response.AuthResponse;
import com.larissafalcao.eventhub_api.entity.Role;
import com.larissafalcao.eventhub_api.exception.EmailAlreadyExistsException;
import com.larissafalcao.eventhub_api.exception.GlobalExceptionHandler;
import com.larissafalcao.eventhub_api.security.JwtService;
import com.larissafalcao.eventhub_api.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("returns 201 when register request is valid")
    void registerReturns201WhenValid() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .name("Alice")
                .email("alice@email.com")
                .role(Role.USER)
                .build();
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Alice", "alice@email.com", "password123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", is("jwt-token")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    @DisplayName("returns 201 when register admin request is valid")
    void registerAdminReturns201WhenValid() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .name("Admin")
                .email("admin@email.com")
                .role(Role.ADMIN)
                .build();
        when(authService.registerAdmin(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/admin/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Admin", "admin@email.com", "password123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", is("jwt-token")))
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    @DisplayName("returns 409 when email is already registered")
    void registerReturns409WhenEmailAlreadyExists() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("alice@email.com"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Alice", "alice@email.com", "password123")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("EMAIL_ALREADY_EXISTS")));
    }

    @Test
    @DisplayName("returns 200 when login request is valid")
    void loginReturns200WhenValid() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .name("Alice")
                .email("alice@email.com")
                .role(Role.USER)
                .build();
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("alice@email.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("jwt-token")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    @DisplayName("returns 400 when register password is too short")
    void registerReturns400WhenPasswordIsTooShort() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Alice", "alice@email.com", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")));
    }

    private static String registerJson(String name, String email, String password) {
        return """
                {
                  "name": "%s",
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(name, email, password);
    }

    private static String loginJson(String email, String password) {
        return """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);
    }
}
