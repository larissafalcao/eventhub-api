package com.larissafalcao.eventhub_api.controller;

import com.larissafalcao.eventhub_api.config.SecurityConfig;
import com.larissafalcao.eventhub_api.security.JwtAuthenticationFilter;
import com.larissafalcao.eventhub_api.security.JwtService;
import com.larissafalcao.eventhub_api.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("returns 401 when registering admin without authentication")
    void registerAdminReturns401WithoutAuthentication() throws Exception {
        mockMvc.perform(post("/auth/admin/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Admin", "admin@email.com", "password123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("AUTHENTICATION_FAILED")));
    }

    @Test
    @DisplayName("returns 403 when registering admin with user role")
    void registerAdminReturns403ForUserRole() throws Exception {
        mockMvc.perform(post("/auth/admin/register")
                        .with(user("user@email.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Admin", "admin@email.com", "password123")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("ACCESS_DENIED")));
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
}
