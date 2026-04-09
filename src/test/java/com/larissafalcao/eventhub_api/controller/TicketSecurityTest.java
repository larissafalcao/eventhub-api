package com.larissafalcao.eventhub_api.controller;

import com.larissafalcao.eventhub_api.config.SecurityConfig;
import com.larissafalcao.eventhub_api.security.JwtAuthenticationFilter;
import com.larissafalcao.eventhub_api.security.JwtService;
import com.larissafalcao.eventhub_api.service.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class TicketSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("returns 401 when purchasing ticket without authentication")
    void purchaseTicketReturns401WithoutAuthentication() throws Exception {
        mockMvc.perform(post("/events/1/tickets"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("AUTHENTICATION_FAILED")));
    }

    @Test
    @DisplayName("returns 403 when admin tries to purchase ticket")
    void purchaseTicketReturns403ForAdminRole() throws Exception {
        mockMvc.perform(post("/events/1/tickets")
                        .with(user("admin@email.com").roles("ADMIN")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("ACCESS_DENIED")));
    }

    @Test
    @DisplayName("returns 403 when admin tries to list own tickets")
    void listMyTicketsReturns403ForAdminRole() throws Exception {
        mockMvc.perform(get("/participant/tickets")
                        .with(user("admin@email.com").roles("ADMIN")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("ACCESS_DENIED")));
    }
}
