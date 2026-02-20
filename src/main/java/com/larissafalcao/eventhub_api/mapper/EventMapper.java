package com.larissafalcao.eventhub_api.mapper;

import com.larissafalcao.eventhub_api.dto.request.CreateEventRequest;
import com.larissafalcao.eventhub_api.dto.request.UpdateEventRequest;
import com.larissafalcao.eventhub_api.dto.response.EventResponse;
import com.larissafalcao.eventhub_api.entity.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public Event toEntity(CreateEventRequest request) {
        return Event.builder()
                .name(request.getName())
                .date(request.getDate())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .build();
    }

    public void updateEntity(Event existingEvent, UpdateEventRequest request) {
        existingEvent.setName(request.getName());
        existingEvent.setDate(request.getDate());
        existingEvent.setLocation(request.getLocation());
        existingEvent.setCapacity(request.getCapacity());
    }

    public EventResponse toResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .date(event.getDate())
                .location(event.getLocation())
                .capacity(event.getCapacity())
                .build();
    }
}
