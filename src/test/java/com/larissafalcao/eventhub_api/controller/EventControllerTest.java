package com.larissafalcao.eventhub_api.controller;

import com.larissafalcao.eventhub_api.dto.request.CreateEventRequest;
import com.larissafalcao.eventhub_api.dto.request.UpdateEventRequest;
import com.larissafalcao.eventhub_api.dto.response.EventResponse;
import com.larissafalcao.eventhub_api.exception.GlobalExceptionHandler;
import com.larissafalcao.eventhub_api.exception.ResourceNotFoundException;
import com.larissafalcao.eventhub_api.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@Import(GlobalExceptionHandler.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);

    @Test
    @DisplayName("returns 201 when valid request")
    void postEventsReturns201WhenValid() throws Exception {
        EventResponse response = EventResponse.builder()
                .id(1L)
                .name("Concert")
                .date(FUTURE_DATE)
                .location("Arena")
                .capacity(1000)
                .build();
        when(eventService.createEvent(any(CreateEventRequest.class))).thenReturn(response);

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("Concert", FUTURE_DATE.toString(), "Arena", 1000)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Concert")));
        verify(eventService).createEvent(any(CreateEventRequest.class));
    }

    @Test
    @DisplayName("returns 400 when name is blank")
    void postEventsReturns400WhenNameBlank() throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("", FUTURE_DATE.toString(), "Arena", 100)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("returns 400 when date is in the past")
    void postEventsReturns400WhenDateInPast() throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("Concert", LocalDate.now().minusDays(1).toString(), "Arena", 100)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("returns 200 and paginated list of events")
    void getEventsReturns200AndPaginatedList() throws Exception {
        EventResponse event = EventResponse.builder()
                .id(1L)
                .name("Meetup")
                .date(FUTURE_DATE)
                .location("Office")
                .capacity(50)
                .build();
        Page<EventResponse> page = new PageImpl<>(List.of(event));
        when(eventService.listEvents(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Meetup")));
    }

    @Test
    @DisplayName("returns 200 when event found")
    void getEventsByIdReturns200WhenFound() throws Exception {
        Long id = 1L;
        EventResponse response = EventResponse.builder()
                .id(id)
                .name("Workshop")
                .date(FUTURE_DATE)
                .location("Room A")
                .capacity(30)
                .build();
        when(eventService.getEventById(id)).thenReturn(response);

        mockMvc.perform(get("/events/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Workshop")));
    }

    @Test
    @DisplayName("returns 404 when event not found")
    void getEventsByIdReturns404WhenNotFound() throws Exception {
        Long id = 999L;
        when(eventService.getEventById(id)).thenThrow(new ResourceNotFoundException("Event not found with id: 999"));

        mockMvc.perform(get("/events/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")));
    }

    @Test
    @DisplayName("returns 200 when valid request")
    void putEventsByIdReturns200WhenValid() throws Exception {
        Long id = 1L;
        EventResponse response = EventResponse.builder()
                .id(id)
                .name("Updated")
                .date(FUTURE_DATE)
                .location("New Place")
                .capacity(200)
                .build();
        when(eventService.updateEvent(eq(id), any(UpdateEventRequest.class))).thenReturn(response);

        mockMvc.perform(put("/events/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("Updated", FUTURE_DATE.toString(), "New Place", 200)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated")));
        verify(eventService).updateEvent(eq(id), any(UpdateEventRequest.class));
    }

    @Test
    @DisplayName("returns 400 when name is blank")
    void putEventsByIdReturns400WhenNameBlank() throws Exception {
        mockMvc.perform(put("/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("", FUTURE_DATE.toString(), "Arena", 100)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("returns 404 when event not found")
    void putEventsByIdReturns404WhenNotFound() throws Exception {
        Long id = 999L;
        when(eventService.updateEvent(eq(id), any(UpdateEventRequest.class)))
                .thenThrow(new ResourceNotFoundException("Event not found with id: 999"));

        mockMvc.perform(put("/events/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("Name", FUTURE_DATE.toString(), "Place", 10)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 204 when event deleted")
    void deleteEventsByIdReturns204WhenDeleted() throws Exception {
        Long id = 1L;

        mockMvc.perform(delete("/events/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(eventService).deleteEvent(id);
    }

    @Test
    @DisplayName("returns 404 when event not found")
    void deleteEventsByIdReturns404WhenNotFound() throws Exception {
        Long id = 999L;
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Event not found with id: 999"))
                .when(eventService).deleteEvent(id);

        mockMvc.perform(delete("/events/{id}", id))
                .andExpect(status().isNotFound());
    }

    private static String eventJson(String name, String date, String location, Integer capacity) {
        return """
                {
                  "name": "%s",
                  "date": "%s",
                  "location": "%s",
                  "capacity": %d
                }
                """.formatted(name, date, location, capacity);
    }
}
