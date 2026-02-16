package com.larissafalcao.eventhub_api.service;

import com.larissafalcao.eventhub_api.dto.request.CreateParticipantRequest;
import com.larissafalcao.eventhub_api.dto.response.ParticipantResponse;
import com.larissafalcao.eventhub_api.entity.Participant;
import com.larissafalcao.eventhub_api.mapper.ParticipantMapper;
import com.larissafalcao.eventhub_api.repository.ParticipantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final ParticipantMapper participantMapper;

    public ParticipantService(ParticipantRepository participantRepository, ParticipantMapper participantMapper) {
        this.participantRepository = participantRepository;
        this.participantMapper = participantMapper;
    }

    @Transactional
    public ParticipantResponse createParticipant(CreateParticipantRequest request) {
        Participant participant = participantMapper.toEntity(request);
        Participant savedParticipant = participantRepository.save(participant);
        return participantMapper.toResponse(savedParticipant);
    }
}
