package com.larissafalcao.eventhub_api.service;

import com.larissafalcao.eventhub_api.config.CacheConfig;
import com.larissafalcao.eventhub_api.dto.request.CreateEventRequest;
import com.larissafalcao.eventhub_api.dto.request.UpdateEventRequest;
import com.larissafalcao.eventhub_api.dto.response.EventResponse;
import com.larissafalcao.eventhub_api.entity.Event;
import com.larissafalcao.eventhub_api.exception.ResourceNotFoundException;
import com.larissafalcao.eventhub_api.mapper.EventMapper;
import com.larissafalcao.eventhub_api.repository.EventRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

    private static final String EVENT_NOT_FOUND = "Event not found with id: %d";

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public EventService(EventRepository eventRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
    }

    @CacheEvict(cacheNames = CacheConfig.EVENTS_CACHE_NAME, allEntries = true)
    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        Event event = eventMapper.toEntity(request);
        Event saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }

    @Cacheable(
            cacheNames = CacheConfig.EVENTS_CACHE_NAME,
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    @Transactional(readOnly = true)
    public Page<EventResponse> listEvents(Pageable pageable) {
        return eventRepository.findAll(pageable)
                .map(eventMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        Event event = findEventById(id);
        return eventMapper.toResponse(event);
    }

    @CacheEvict(cacheNames = CacheConfig.EVENTS_CACHE_NAME, allEntries = true)
    @Transactional
    public EventResponse updateEvent(Long id, UpdateEventRequest request) {
        Event existingEvent = findEventById(id);
        eventMapper.updateEntity(existingEvent, request);
        Event updated = eventRepository.save(existingEvent);
        return eventMapper.toResponse(updated);
    }

    @CacheEvict(cacheNames = CacheConfig.EVENTS_CACHE_NAME, allEntries = true)
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
