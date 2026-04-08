package com.larissafalcao.eventhub_api.event;

import java.time.Instant;

public record TicketPurchasedEvent(
        Long ticketId,
        String participantName,
        String participantEmail,
        String eventName,
        Instant purchasedAt) {
}
