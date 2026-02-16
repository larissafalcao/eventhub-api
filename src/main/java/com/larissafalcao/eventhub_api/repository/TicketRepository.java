package com.larissafalcao.eventhub_api.repository;

import com.larissafalcao.eventhub_api.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    long countByEventId(Long eventId);

    List<Ticket> findByParticipantIdOrderByPurchasedAtDesc(Long participantId);
}
