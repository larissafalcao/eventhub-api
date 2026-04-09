package com.larissafalcao.eventhub_api.controller;

import com.larissafalcao.eventhub_api.config.SecurityConfig;
import com.larissafalcao.eventhub_api.dto.request.CreateEventRequest;
import com.larissafalcao.eventhub_api.dto.response.EventResponse;
import com.larissafalcao.eventhub_api.security.JwtAuthenticationFilter;
import com.larissafalcao.eventhub_api.security.JwtService;
import com.larissafalcao.eventhub_api.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class EventSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("returns 401 when creating event without authentication")
    void createEventReturns401WithoutAuthentication() throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("AUTHENTICATION_FAILED")));
    }

    @Test
    @DisplayName("returns 403 when creating event with user role")
    void createEventReturns403ForUserRole() throws Exception {
        mockMvc.perform(post("/events")
                        .with(user("user@email.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("ACCESS_DENIED")));
    }

    @Test
    @DisplayName("returns 201 when creating event with admin role")
    void createEventReturns201ForAdminRole() throws Exception {
        EventResponse response = EventResponse.builder()
                .id(1L)
                .name("Concert")
                .date(LocalDate.now().plusDays(7))
                .location("Arena")
                .capacity(100)
                .build();
        when(eventService.createEvent(any(CreateEventRequest.class))).thenReturn(response);

        mockMvc.perform(post("/events")
                        .with(user("admin@email.com").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)));
    }

    private static String eventJson() {
        return """
                {
                  "name": "Concert",
                  "date": "%s",
                  "location": "Arena",
                  "capacity": 100
                }
                """.formatted(LocalDate.now().plusDays(7));
    }
}
