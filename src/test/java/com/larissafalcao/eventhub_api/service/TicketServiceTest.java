package com.larissafalcao.eventhub_api.service;

import com.larissafalcao.eventhub_api.dto.request.PurchaseTicketRequest;
import com.larissafalcao.eventhub_api.dto.response.TicketResponse;
import com.larissafalcao.eventhub_api.entity.Event;
import com.larissafalcao.eventhub_api.entity.Participant;
import com.larissafalcao.eventhub_api.entity.Ticket;
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

import java.time.LocalDate;
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
        when(ticketRepository.countByEventId(eventId)).thenReturn(99L);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setId(123L);
            return ticket;
        });

        TicketResponse response = ticketService.purchaseTicket(eventId, request);

        assertThat(response.getId()).isEqualTo(123L);
        assertThat(response.getEventId()).isEqualTo(eventId);
        assertThat(response.getParticipantId()).isEqualTo(participantId);
        assertThat(response.getPurchasedAt()).isNotNull();
        verify(ticketRepository).save(any(Ticket.class));
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
    @DisplayName("purchaseTicket: throws EventFullException when event is full")
    void purchaseTicketThrowsWhenEventIsFull() {
        Long eventId = 1L;
        Long participantId = 10L;
        Event event = createEvent(eventId, 100);
        Participant participant = createParticipant(participantId, "Alice", "alice@email.com");
        PurchaseTicketRequest request = PurchaseTicketRequest.builder()
                .participantId(participantId)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(ticketRepository.countByEventId(eventId)).thenReturn(100L);

        assertThatThrownBy(() -> ticketService.purchaseTicket(eventId, request))
                .isInstanceOf(EventFullException.class)
                .hasMessage("Event with id 1 is full");

        verify(ticketRepository, never()).save(any(Ticket.class));
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
