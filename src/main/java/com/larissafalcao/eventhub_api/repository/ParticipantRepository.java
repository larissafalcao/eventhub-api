package com.larissafalcao.eventhub_api.repository;

import com.larissafalcao.eventhub_api.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
}
