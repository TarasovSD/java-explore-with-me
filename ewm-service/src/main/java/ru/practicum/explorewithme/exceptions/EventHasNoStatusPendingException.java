package ru.practicum.explorewithme.exceptions;

public class EventHasNoStatusPendingException extends RuntimeException {
    public EventHasNoStatusPendingException(String message) {
        super(message);
    }
}
