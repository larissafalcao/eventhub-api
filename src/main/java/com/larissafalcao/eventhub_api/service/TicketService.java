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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TicketService {

    private static final String EVENT_NOT_FOUND = "Event not found with id: %d";
    private static final String PARTICIPANT_NOT_FOUND = "Participant not found with id: %d";

    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    public TicketService(
            EventRepository eventRepository,
            ParticipantRepository participantRepository,
            TicketRepository ticketRepository,
            TicketMapper ticketMapper) {
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
    }

    @Transactional
    public TicketResponse purchaseTicket(Long eventId, PurchaseTicketRequest request) {
        Event event = findEventById(eventId);
        Participant participant = findParticipantById(request.getParticipantId());

        long soldTickets = ticketRepository.countByEventId(eventId);
        if (soldTickets >= event.getCapacity()) {
            throw new EventFullException(eventId);
        }

        Ticket ticket = Ticket.builder()
                .event(event)
                .participant(participant)
                .purchasedAt(Instant.now())
                .build();
        Ticket savedTicket = ticketRepository.save(ticket);

        return ticketMapper.toResponse(savedTicket);
    }

    public List<TicketResponse> listTicketsByParticipant(Long participantId) {
        Participant participant = findParticipantById(participantId);
        return ticketRepository.findByParticipantIdOrderByPurchasedAtDesc(participant.getId()).stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(EVENT_NOT_FOUND, eventId)));
    }

    private Participant findParticipantById(Long participantId) {
        return participantRepository.findById(participantId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(String.format(PARTICIPANT_NOT_FOUND, participantId)));
    }
}
