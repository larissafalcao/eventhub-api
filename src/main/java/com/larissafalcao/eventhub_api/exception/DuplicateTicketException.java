package com.larissafalcao.eventhub_api.exception;

public class DuplicateTicketException extends RuntimeException {

    public DuplicateTicketException(Long eventId, Long participantId) {
        super(String.format("Participant %d already has a ticket for event %d", participantId, eventId));
    }
}
