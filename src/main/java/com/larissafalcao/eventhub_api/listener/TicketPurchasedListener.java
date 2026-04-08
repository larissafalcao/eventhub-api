package com.larissafalcao.eventhub_api.listener;

import com.larissafalcao.eventhub_api.event.TicketPurchasedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TicketPurchasedListener {

    private static final Logger log = LoggerFactory.getLogger(TicketPurchasedListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketPurchased(TicketPurchasedEvent event) {
        log.info(
                "[EMAIL] Confirmation sent to {} ({}) for event \"{}\" - Ticket #{} at {}",
                event.participantName(),
                event.participantEmail(),
                event.eventName(),
                event.ticketId(),
                event.purchasedAt());
    }
}
