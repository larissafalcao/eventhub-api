package com.larissafalcao.eventhub_api.controller;

import com.larissafalcao.eventhub_api.dto.request.PurchaseTicketRequest;
import com.larissafalcao.eventhub_api.dto.response.TicketResponse;
import com.larissafalcao.eventhub_api.exception.DuplicateTicketException;
import com.larissafalcao.eventhub_api.exception.EventFullException;
import com.larissafalcao.eventhub_api.exception.GlobalExceptionHandler;
import com.larissafalcao.eventhub_api.exception.ResourceNotFoundException;
import com.larissafalcao.eventhub_api.service.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@Import(GlobalExceptionHandler.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @Test
    @DisplayName("returns 201 when purchase is valid")
    void postTicketPurchaseReturns201WhenValid() throws Exception {
        Long eventId = 1L;
        TicketResponse response = TicketResponse.builder()
                .id(100L)
                .eventId(eventId)
                .participantId(10L)
                .purchasedAt(Instant.parse("2026-02-16T12:00:00Z"))
                .build();
        when(ticketService.purchaseTicket(eq(eventId), any(PurchaseTicketRequest.class))).thenReturn(response);

        mockMvc.perform(post("/events/{eventId}/tickets", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseTicketJson(10L)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.eventId", is(1)))
                .andExpect(jsonPath("$.participantId", is(10)));

        verify(ticketService).purchaseTicket(eq(eventId), any(PurchaseTicketRequest.class));
    }

    @Test
    @DisplayName("returns 400 when participantId is null")
    void postTicketPurchaseReturns400WhenParticipantIdNull() throws Exception {
        mockMvc.perform(post("/events/{eventId}/tickets", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "participantId": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")));
    }

    @Test
    @DisplayName("returns 404 when event is not found")
    void postTicketPurchaseReturns404WhenEventNotFound() throws Exception {
        Long eventId = 999L;
        when(ticketService.purchaseTicket(eq(eventId), any(PurchaseTicketRequest.class)))
                .thenThrow(new ResourceNotFoundException("Event not found with id: 999"));

        mockMvc.perform(post("/events/{eventId}/tickets", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseTicketJson(10L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")));
    }

    @Test
    @DisplayName("returns 409 when event is full")
    void postTicketPurchaseReturns409WhenEventIsFull() throws Exception {
        Long eventId = 1L;
        when(ticketService.purchaseTicket(eq(eventId), any(PurchaseTicketRequest.class)))
                .thenThrow(new EventFullException(eventId));

        mockMvc.perform(post("/events/{eventId}/tickets", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseTicketJson(10L)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("EVENT_FULL")));
    }

    @Test
    @DisplayName("returns 409 when ticket is duplicate")
    void postTicketPurchaseReturns409WhenDuplicate() throws Exception {
        Long eventId = 1L;
        when(ticketService.purchaseTicket(eq(eventId), any(PurchaseTicketRequest.class)))
                .thenThrow(new DuplicateTicketException(eventId, 10L));

        mockMvc.perform(post("/events/{eventId}/tickets", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseTicketJson(10L)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("DUPLICATE_TICKET")));
    }

    @Test
    @DisplayName("returns 200 and ticket history")
    void getParticipantTicketsReturns200AndHistory() throws Exception {
        Long participantId = 10L;
        TicketResponse ticket = TicketResponse.builder()
                .id(100L)
                .eventId(1L)
                .participantId(participantId)
                .purchasedAt(Instant.parse("2026-02-16T12:00:00Z"))
                .build();
        when(ticketService.listTicketsByParticipant(participantId)).thenReturn(List.of(ticket));

        mockMvc.perform(get("/participants/{participantId}/tickets", participantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(100)))
                .andExpect(jsonPath("$[0].participantId", is(10)));
    }

    @Test
    @DisplayName("returns 404 when participant is not found")
    void getParticipantTicketsReturns404WhenParticipantNotFound() throws Exception {
        Long participantId = 999L;
        when(ticketService.listTicketsByParticipant(participantId))
                .thenThrow(new ResourceNotFoundException("Participant not found with id: 999"));

        mockMvc.perform(get("/participants/{participantId}/tickets", participantId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")));
    }

    private static String purchaseTicketJson(Long participantId) {
        return """
                {
                  "participantId": %d
                }
                """.formatted(participantId);
    }
}
