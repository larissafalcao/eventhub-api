package com.larissafalcao.eventhub_api.mapper;

import com.larissafalcao.eventhub_api.dto.response.TicketResponse;
import com.larissafalcao.eventhub_api.entity.Ticket;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public TicketResponse toResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .eventId(ticket.getEvent().getId())
                .participantId(ticket.getParticipant().getId())
                .purchasedAt(ticket.getPurchasedAt())
                .build();
    }
}
