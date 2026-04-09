package com.larissafalcao.eventhub_api.controller;

import com.larissafalcao.eventhub_api.dto.response.TicketResponse;
import com.larissafalcao.eventhub_api.entity.Participant;
import com.larissafalcao.eventhub_api.entity.Role;
import com.larissafalcao.eventhub_api.entity.User;
import com.larissafalcao.eventhub_api.exception.DuplicateTicketException;
import com.larissafalcao.eventhub_api.exception.EventFullException;
import com.larissafalcao.eventhub_api.exception.ResourceNotFoundException;
import com.larissafalcao.eventhub_api.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    private TicketController ticketController;

    @BeforeEach
    void setUp() {
        ticketController = new TicketController(ticketService);
    }

    @Test
    @DisplayName("returns 201 when purchase is valid")
    void postTicketPurchaseReturns201WhenValid() {
        Long eventId = 1L;
        User authenticatedUser = authenticatedUser(10L);
        TicketResponse response = TicketResponse.builder()
                .id(100L)
                .eventId(eventId)
                .participantId(10L)
                .purchasedAt(Instant.parse("2026-02-16T12:00:00Z"))
                .build();
        when(ticketService.purchaseTicket(eventId, authenticatedUser)).thenReturn(response);

        ResponseEntity<TicketResponse> entity = ticketController.purchaseTicket(eventId, authenticatedUser);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().getId()).isEqualTo(100L);
        assertThat(entity.getBody().getParticipantId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("returns 404 when event is not found")
    void postTicketPurchaseReturns404WhenEventNotFound() {
        Long eventId = 999L;
        User authenticatedUser = authenticatedUser(10L);
        when(ticketService.purchaseTicket(eventId, authenticatedUser))
                .thenThrow(new ResourceNotFoundException("Event not found with id: 999"));

        assertThatThrownBy(() -> ticketController.purchaseTicket(eventId, authenticatedUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Event not found with id: 999");
    }

    @Test
    @DisplayName("returns 409 when event is full")
    void postTicketPurchaseReturns409WhenEventIsFull() {
        Long eventId = 1L;
        User authenticatedUser = authenticatedUser(10L);
        when(ticketService.purchaseTicket(eventId, authenticatedUser))
                .thenThrow(new EventFullException(eventId));

        assertThatThrownBy(() -> ticketController.purchaseTicket(eventId, authenticatedUser))
                .isInstanceOf(EventFullException.class)
                .hasMessage("Event with id 1 is full");
    }

    @Test
    @DisplayName("returns 409 when ticket is duplicate")
    void postTicketPurchaseReturns409WhenDuplicate() {
        Long eventId = 1L;
        User authenticatedUser = authenticatedUser(10L);
        when(ticketService.purchaseTicket(eventId, authenticatedUser))
                .thenThrow(new DuplicateTicketException(eventId, 10L));

        assertThatThrownBy(() -> ticketController.purchaseTicket(eventId, authenticatedUser))
                .isInstanceOf(DuplicateTicketException.class)
                .hasMessage("Participant 10 already has a ticket for event 1");
    }

    @Test
    @DisplayName("returns 200 and ticket history for authenticated user")
    void getMyTicketsReturns200AndHistory() {
        Long participantId = 10L;
        User authenticatedUser = authenticatedUser(participantId);
        TicketResponse ticket = TicketResponse.builder()
                .id(100L)
                .eventId(1L)
                .participantId(participantId)
                .purchasedAt(Instant.parse("2026-02-16T12:00:00Z"))
                .build();
        when(ticketService.listTicketsByAuthenticatedUser(authenticatedUser)).thenReturn(List.of(ticket));

        ResponseEntity<List<TicketResponse>> entity = ticketController.listMyTickets(authenticatedUser);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).hasSize(1);
        assertThat(entity.getBody().getFirst().getParticipantId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("returns 404 when authenticated participant is not found")
    void getMyTicketsReturns404WhenParticipantNotFound() {
        Long participantId = 999L;
        User authenticatedUser = authenticatedUser(participantId);
        when(ticketService.listTicketsByAuthenticatedUser(authenticatedUser))
                .thenThrow(new ResourceNotFoundException("Participant not found with id: 999"));

        assertThatThrownBy(() -> ticketController.listMyTickets(authenticatedUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Participant not found with id: 999");
    }

    @Test
    @DisplayName("throws 403-equivalent exception when user has no participant profile")
    void postTicketPurchaseThrowsWhenUserHasNoParticipantProfile() {
        User adminUser = adminUserWithoutParticipant();
        when(ticketService.purchaseTicket(1L, adminUser))
                .thenThrow(new AccessDeniedException("Only users with a participant profile can access tickets"));

        assertThatThrownBy(() -> ticketController.purchaseTicket(1L, adminUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Only users with a participant profile can access tickets");
    }

    private static User authenticatedUser(Long participantId) {
        return User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@email.com")
                .password("encoded-password")
                .role(Role.USER)
                .participant(Participant.builder()
                        .id(participantId)
                        .name("Alice")
                        .email("alice@email.com")
                        .build())
                .build();
    }

    private static User adminUserWithoutParticipant() {
        return User.builder()
                .id(99L)
                .name("System Admin")
                .email("admin@eventhub.local")
                .password("encoded-password")
                .role(Role.ADMIN)
                .build();
    }
}
