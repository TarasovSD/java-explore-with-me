package ru.practicum.explorewithme.exceptions;

public class RequestNotCreatedException extends RuntimeException {
    public RequestNotCreatedException(String message) {
        super(message);
    }
}
