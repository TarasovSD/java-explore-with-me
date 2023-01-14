package ru.practicum.explorewithme.exceptions;

public class RequestNotConfirmedException extends RuntimeException {
    public RequestNotConfirmedException(String message) {
        super(message);
    }
}
