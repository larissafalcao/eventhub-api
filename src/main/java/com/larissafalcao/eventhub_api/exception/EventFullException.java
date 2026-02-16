package com.larissafalcao.eventhub_api.exception;

public class EventFullException extends RuntimeException {

    public EventFullException(Long eventId) {
        super(String.format("Event with id %d is full", eventId));
    }
}
