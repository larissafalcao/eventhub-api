package com.larissafalcao.eventhub_api.mapper;

import com.larissafalcao.eventhub_api.dto.request.CreateParticipantRequest;
import com.larissafalcao.eventhub_api.dto.response.ParticipantResponse;
import com.larissafalcao.eventhub_api.entity.Participant;
import org.springframework.stereotype.Component;

@Component
public class ParticipantMapper {

    public Participant toEntity(CreateParticipantRequest request) {
        return Participant.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();
    }

    public ParticipantResponse toResponse(Participant participant) {
        return ParticipantResponse.builder()
                .id(participant.getId())
                .name(participant.getName())
                .email(participant.getEmail())
                .build();
    }
}
