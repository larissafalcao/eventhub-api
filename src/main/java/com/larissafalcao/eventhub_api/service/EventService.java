package com.larissafalcao.eventhub_api.service;

import com.larissafalcao.eventhub_api.dto.request.CreateEventRequest;
import com.larissafalcao.eventhub_api.dto.request.UpdateEventRequest;
import com.larissafalcao.eventhub_api.dto.response.EventResponse;
import com.larissafalcao.eventhub_api.entity.Event;
import com.larissafalcao.eventhub_api.exception.ResourceNotFoundException;
import com.larissafalcao.eventhub_api.mapper.EventMapper;
import com.larissafalcao.eventhub_api.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {

    private static final String EVENT_NOT_FOUND = "Event not found with id: %d";

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public EventService(EventRepository eventRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
    }

    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        Event event = eventMapper.toEntity(request);
        Event saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }

    public List<EventResponse> listEvents() {
        return eventRepository.findAll().stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    public EventResponse getEventById(Long id) {
        Event event = findEventById(id);
        return eventMapper.toResponse(event);
    }

    @Transactional
    public EventResponse updateEvent(Long id, UpdateEventRequest request) {
        Event existingEvent = findEventById(id);
        Event updatedEvent = eventMapper.toUpdatedEntity(existingEvent, request);
        Event updated = eventRepository.save(updatedEvent);
        return eventMapper.toResponse(updated);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = findEventById(id);
        eventRepository.delete(event);
    }

    private Event findEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(EVENT_NOT_FOUND, id)));
    }
}
