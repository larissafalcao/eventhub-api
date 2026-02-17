package com.larissafalcao.eventhub_api.controller;

import com.larissafalcao.eventhub_api.dto.request.CreateParticipantRequest;
import com.larissafalcao.eventhub_api.dto.response.ParticipantResponse;
import com.larissafalcao.eventhub_api.exception.GlobalExceptionHandler;
import com.larissafalcao.eventhub_api.service.ParticipantService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParticipantController.class)
@Import(GlobalExceptionHandler.class)
class ParticipantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParticipantService participantService;

    @Test
    @DisplayName("returns 201 when valid request")
    void postParticipantsReturns201WhenValid() throws Exception {
        ParticipantResponse response = ParticipantResponse.builder()
                .id(1L)
                .name("Alice")
                .email("alice@email.com")
                .build();
        when(participantService.createParticipant(any(CreateParticipantRequest.class))).thenReturn(response);

        mockMvc.perform(post("/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createParticipantJson("Alice", "alice@email.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Alice")))
                .andExpect(jsonPath("$.email", is("alice@email.com")));

        verify(participantService).createParticipant(any(CreateParticipantRequest.class));
    }

    @Test
    @DisplayName("returns 400 when name is blank")
    void postParticipantsReturns400WhenNameBlank() throws Exception {
        mockMvc.perform(post("/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createParticipantJson("", "alice@email.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")));
    }

    @Test
    @DisplayName("returns 400 when email is invalid")
    void postParticipantsReturns400WhenEmailInvalid() throws Exception {
        mockMvc.perform(post("/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createParticipantJson("Alice", "invalid-email")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")));
    }

    private static String createParticipantJson(String name, String email) {
        return """
                {
                  "name": "%s",
                  "email": "%s"
                }
                """.formatted(name, email);
    }
}
