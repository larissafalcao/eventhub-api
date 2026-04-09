package com.larissafalcao.eventhub_api.service;

import com.larissafalcao.eventhub_api.dto.response.TicketResponse;
import com.larissafalcao.eventhub_api.entity.Event;
import com.larissafalcao.eventhub_api.entity.Participant;
import com.larissafalcao.eventhub_api.entity.Ticket;
import com.larissafalcao.eventhub_api.entity.User;
import com.larissafalcao.eventhub_api.event.TicketPurchasedEvent;
import com.larissafalcao.eventhub_api.exception.DuplicateTicketException;
import com.larissafalcao.eventhub_api.exception.EventFullException;
import com.larissafalcao.eventhub_api.exception.ResourceNotFoundException;
import com.larissafalcao.eventhub_api.mapper.TicketMapper;
import com.larissafalcao.eventhub_api.repository.EventRepository;
import com.larissafalcao.eventhub_api.repository.ParticipantRepository;
import com.larissafalcao.eventhub_api.repository.TicketRepository;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher applicationEventPublisher;

    public TicketService(
            EventRepository eventRepository,
            ParticipantRepository participantRepository,
            TicketRepository ticketRepository,
            TicketMapper ticketMapper,
            ApplicationEventPublisher applicationEventPublisher) {
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public TicketResponse purchaseTicket(Long eventId, Long participantId) {
        Event event = findEventByIdForUpdate(eventId);

        if (event.getCapacity() <= 0) {
            throw new EventFullException(eventId);
        }

        Participant participant = findParticipantById(participantId);

        if (ticketRepository.existsByEventIdAndParticipantId(eventId, participant.getId())) {
            throw new DuplicateTicketException(eventId, participant.getId());
        }

        Ticket ticket = Ticket.builder()
                .event(event)
                .participant(participant)
                .purchasedAt(Instant.now())
                .build();
        Ticket savedTicket = ticketRepository.save(ticket);
        event.setCapacity(event.getCapacity() - 1);
        eventRepository.save(event);
        applicationEventPublisher.publishEvent(new TicketPurchasedEvent(
                savedTicket.getId(),
                participant.getName(),
                participant.getEmail(),
                event.getName(),
                savedTicket.getPurchasedAt()));

        return ticketMapper.toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse purchaseTicket(Long eventId, User authenticatedUser) {
        return purchaseTicket(eventId, authenticatedUser.getParticipant().getId());
    }

    public List<TicketResponse> listTicketsByAuthenticatedUser(User authenticatedUser) {
        Long participantId = authenticatedUser.getParticipant().getId();
        return ticketRepository.findByParticipantIdOrderByPurchasedAtDesc(participantId).stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    private Event findEventByIdForUpdate(Long eventId) {
        return eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(EVENT_NOT_FOUND, eventId)));
    }

    private Participant findParticipantById(Long participantId) {
        return participantRepository.findById(participantId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(String.format(PARTICIPANT_NOT_FOUND, participantId)));
    }
}
