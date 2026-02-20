package com.larissafalcao.eventhub_api.service;

import com.larissafalcao.eventhub_api.dto.request.CreateParticipantRequest;
import com.larissafalcao.eventhub_api.dto.response.ParticipantResponse;
import com.larissafalcao.eventhub_api.entity.Participant;
import com.larissafalcao.eventhub_api.mapper.ParticipantMapper;
import com.larissafalcao.eventhub_api.repository.ParticipantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {

    @Mock
    private ParticipantRepository participantRepository;

    @Spy
    private ParticipantMapper participantMapper = new ParticipantMapper();

    @InjectMocks
    private ParticipantService participantService;

    @Test
    @DisplayName("createParticipant: creates participant and returns response")
    void createParticipantCreatesAndReturnsResponse() {
        CreateParticipantRequest request = CreateParticipantRequest.builder()
                .name("Alice")
                .email("alice@email.com")
                .build();
        Participant saved = Participant.builder()
                .id(1L)
                .name("Alice")
                .email("alice@email.com")
                .build();
        when(participantRepository.save(any(Participant.class))).thenReturn(saved);

        ParticipantResponse response = participantService.createParticipant(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Alice");
        assertThat(response.getEmail()).isEqualTo("alice@email.com");
        verify(participantRepository).save(any(Participant.class));
    }
}
