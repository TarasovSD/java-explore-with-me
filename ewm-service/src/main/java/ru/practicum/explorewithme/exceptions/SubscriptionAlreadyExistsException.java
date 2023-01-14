package ru.practicum.explorewithme.exceptions;

public class SubscriptionAlreadyExistsException extends RuntimeException {
    public SubscriptionAlreadyExistsException(String message) {
        super(message);
    }
}
