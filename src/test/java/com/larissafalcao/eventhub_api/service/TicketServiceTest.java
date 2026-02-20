package com.larissafalcao.eventhub_api.service;

import com.larissafalcao.eventhub_api.dto.request.PurchaseTicketRequest;
import com.larissafalcao.eventhub_api.dto.response.TicketResponse;
import com.larissafalcao.eventhub_api.entity.Event;
import com.larissafalcao.eventhub_api.entity.Participant;
import com.larissafalcao.eventhub_api.entity.Ticket;
import com.larissafalcao.eventhub_api.exception.DuplicateTicketException;
import com.larissafalcao.eventhub_api.exception.EventFullException;
import com.larissafalcao.eventhub_api.exception.ResourceNotFoundException;
import com.larissafalcao.eventhub_api.mapper.TicketMapper;
import com.larissafalcao.eventhub_api.repository.EventRepository;
import com.larissafalcao.eventhub_api.repository.ParticipantRepository;
import com.larissafalcao.eventhub_api.repository.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Spy
    private TicketMapper ticketMapper = new TicketMapper();

    @InjectMocks
    private TicketService ticketService;

    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);

    @Test
    @DisplayName("purchaseTicket: creates ticket when event has available capacity")
    void purchaseTicketCreatesTicketWhenEventHasAvailableCapacity() {
        Long eventId = 1L;
        Long participantId = 10L;
        Event event = createEvent(eventId, 100);
        Participant participant = createParticipant(participantId, "Alice", "alice@email.com");
        PurchaseTicketRequest request = PurchaseTicketRequest.builder()
                .participantId(participantId)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(ticketRepository.existsByEventIdAndParticipantId(eventId, participantId)).thenReturn(false);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            return Ticket.builder()
                    .id(123L)
                    .event(t.getEvent())
                    .participant(t.getParticipant())
                    .purchasedAt(t.getPurchasedAt())
                    .build();
        });

        TicketResponse response = ticketService.purchaseTicket(eventId, request);

        assertThat(response.getId()).isEqualTo(123L);
        assertThat(response.getEventId()).isEqualTo(eventId);
        assertThat(response.getParticipantId()).isEqualTo(participantId);
        assertThat(response.getPurchasedAt()).isNotNull();
        assertThat(event.getCapacity()).isEqualTo(99);
        verify(ticketRepository).save(any(Ticket.class));
        verify(eventRepository).save(event);
    }

    @Test
    @DisplayName("purchaseTicket: throws EventFullException before looking up participant")
    void purchaseTicketThrowsEventFullBeforeParticipantLookup() {
        Long eventId = 1L;
        Event event = createEvent(eventId, 0);
        PurchaseTicketRequest request = PurchaseTicketRequest.builder()
                .participantId(10L)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> ticketService.purchaseTicket(eventId, request))
                .isInstanceOf(EventFullException.class)
                .hasMessage("Event with id 1 is full");

        verify(participantRepository, never()).findById(any());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("purchaseTicket: throws ResourceNotFoundException when event does not exist")
    void purchaseTicketThrowsWhenEventDoesNotExist() {
        Long eventId = 999L;
        PurchaseTicketRequest request = PurchaseTicketRequest.builder()
                .participantId(1L)
                .build();
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.purchaseTicket(eventId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Event not found with id: 999");

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("purchaseTicket: throws ResourceNotFoundException when participant does not exist")
    void purchaseTicketThrowsWhenParticipantDoesNotExist() {
        Long eventId = 1L;
        Long participantId = 999L;
        Event event = createEvent(eventId, 100);
        PurchaseTicketRequest request = PurchaseTicketRequest.builder()
                .participantId(participantId)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(participantRepository.findById(participantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.purchaseTicket(eventId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Participant not found with id: 999");

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("purchaseTicket: throws DuplicateTicketException when ticket already exists")
    void purchaseTicketThrowsWhenDuplicate() {
        Long eventId = 1L;
        Long participantId = 10L;
        Event event = createEvent(eventId, 100);
        Participant participant = createParticipant(participantId, "Alice", "alice@email.com");
        PurchaseTicketRequest request = PurchaseTicketRequest.builder()
                .participantId(participantId)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(ticketRepository.existsByEventIdAndParticipantId(eventId, participantId)).thenReturn(true);

        assertThatThrownBy(() -> ticketService.purchaseTicket(eventId, request))
                .isInstanceOf(DuplicateTicketException.class)
                .hasMessage("Participant 10 already has a ticket for event 1");

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("listTicketsByParticipant: returns tickets ordered by purchase date")
    void listTicketsByParticipantReturnsTickets() {
        Long participantId = 10L;
        Participant participant = createParticipant(participantId, "Alice", "alice@email.com");
        Event event = createEvent(1L, 100);
        Ticket ticket = Ticket.builder()
                .id(50L)
                .event(event)
                .participant(participant)
                .purchasedAt(Instant.parse("2026-02-16T12:00:00Z"))
                .build();

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(ticketRepository.findByParticipantIdOrderByPurchasedAtDesc(participantId))
                .thenReturn(List.of(ticket));

        List<TicketResponse> result = ticketService.listTicketsByParticipant(participantId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(50L);
        assertThat(result.get(0).getEventId()).isEqualTo(1L);
        assertThat(result.get(0).getParticipantId()).isEqualTo(participantId);
    }

    @Test
    @DisplayName("listTicketsByParticipant: throws ResourceNotFoundException when participant not found")
    void listTicketsByParticipantThrowsWhenNotFound() {
        Long participantId = 999L;
        when(participantRepository.findById(participantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.listTicketsByParticipant(participantId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Participant not found with id: 999");
    }

    private static Event createEvent(Long id, Integer capacity) {
        return Event.builder()
                .id(id)
                .name("JavaConf")
                .date(FUTURE_DATE)
                .location("Main Hall")
                .capacity(capacity)
                .build();
    }

    private static Participant createParticipant(Long id, String name, String email) {
        return Participant.builder()
                .id(id)
                .name(name)
                .email(email)
                .build();
    }
}
