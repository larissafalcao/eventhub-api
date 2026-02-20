package com.larissafalcao.eventhub_api.service;

import com.larissafalcao.eventhub_api.dto.request.CreateEventRequest;
import com.larissafalcao.eventhub_api.dto.request.UpdateEventRequest;
import com.larissafalcao.eventhub_api.dto.response.EventResponse;
import com.larissafalcao.eventhub_api.entity.Event;
import com.larissafalcao.eventhub_api.exception.ResourceNotFoundException;
import com.larissafalcao.eventhub_api.mapper.EventMapper;
import com.larissafalcao.eventhub_api.repository.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Spy
    private EventMapper eventMapper = new EventMapper();

    @InjectMocks
    private EventService eventService;

    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);

    @Test
    @DisplayName("createEvent: creates event and returns response")
    void createEventCreatesEventAndReturnsResponse() {
        CreateEventRequest request = createRequest("Concert", FUTURE_DATE, "Arena", 1000);
        Event saved = Event.builder()
                .id(1L)
                .name("Concert")
                .date(FUTURE_DATE)
                .location("Arena")
                .capacity(1000)
                .build();
        when(eventRepository.save(any(Event.class))).thenReturn(saved);

        EventResponse response = eventService.createEvent(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Concert");
        assertThat(response.getDate()).isEqualTo(FUTURE_DATE);
        assertThat(response.getLocation()).isEqualTo("Arena");
        assertThat(response.getCapacity()).isEqualTo(1000);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("listEvents: returns paginated events")
    void listEventsReturnsPaginatedEvents() {
        Event event = Event.builder()
                .id(1L)
                .name("Meetup")
                .date(FUTURE_DATE)
                .location("Office")
                .capacity(50)
                .build();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Event> page = new PageImpl<>(List.of(event), pageable, 1);
        when(eventRepository.findAll(pageable)).thenReturn(page);

        Page<EventResponse> result = eventService.listEvents(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Meetup");
        verify(eventRepository).findAll(pageable);
    }

    @Test
    @DisplayName("getEventById: returns event when found")
    void getEventByIdReturnsEventWhenFound() {
        Long id = 1L;
        Event event = Event.builder()
                .id(id)
                .name("Workshop")
                .date(FUTURE_DATE)
                .location("Room A")
                .capacity(30)
                .build();
        when(eventRepository.findById(id)).thenReturn(Optional.of(event));

        EventResponse response = eventService.getEventById(id);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("Workshop");
        verify(eventRepository).findById(id);
    }

    @Test
    @DisplayName("getEventById: throws ResourceNotFoundException when not found")
    void getEventByIdThrowsWhenNotFound() {
        Long id = 999L;
        when(eventRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Event not found with id: 999");
    }

    @Test
    @DisplayName("updateEvent: updates existing entity fields and returns response")
    void updateEventUpdatesExistingEntityAndReturnsResponse() {
        Long id = 1L;
        Event existing = Event.builder()
                .id(id)
                .name("Old Name")
                .date(FUTURE_DATE)
                .location("Old Location")
                .capacity(10)
                .build();
        UpdateEventRequest request = UpdateEventRequest.builder()
                .name("New Name")
                .date(FUTURE_DATE.plusDays(1))
                .location("New Location")
                .capacity(20)
                .build();
        when(eventRepository.findById(id)).thenReturn(Optional.of(existing));
        when(eventRepository.save(existing)).thenReturn(existing);

        EventResponse response = eventService.updateEvent(id, request);

        assertThat(response.getName()).isEqualTo("New Name");
        assertThat(response.getCapacity()).isEqualTo(20);
        assertThat(existing.getName()).isEqualTo("New Name");
        verify(eventRepository).save(existing);
    }

    @Test
    @DisplayName("updateEvent: throws ResourceNotFoundException when not found")
    void updateEventThrowsWhenNotFound() {
        Long id = 999L;
        when(eventRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.updateEvent(id, UpdateEventRequest.builder().build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteEvent: deletes event when found")
    void deleteEventDeletesEventWhenFound() {
        Long id = 1L;
        Event event = Event.builder()
                .id(id)
                .name("To Delete")
                .date(FUTURE_DATE)
                .location("Somewhere")
                .capacity(5)
                .build();
        when(eventRepository.findById(id)).thenReturn(Optional.of(event));

        eventService.deleteEvent(id);

        verify(eventRepository).delete(event);
    }

    @Test
    @DisplayName("deleteEvent: throws ResourceNotFoundException when not found")
    void deleteEventThrowsWhenNotFound() {
        Long id = 999L;
        when(eventRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.deleteEvent(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static CreateEventRequest createRequest(String name, LocalDate date, String location, Integer capacity) {
        return CreateEventRequest.builder()
                .name(name)
                .date(date)
                .location(location)
                .capacity(capacity)
                .build();
    }
}
