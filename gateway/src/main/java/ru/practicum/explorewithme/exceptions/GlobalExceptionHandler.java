package ru.practicum.explorewithme.exceptions;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleThrowable(final Throwable e) {
        ErrorDto errorDto = new ErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
        log.info(e.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleNoSuchElementException(final NoSuchElementException e) {
        log.info(e.getMessage());
        return new ResponseEntity<>("Объект отсутствует в БД", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleWebClientResponseException(final WebClientResponseException e) {
        log.info(e.getMessage());
        ErrorDto errorDto = new ErrorDto(e.getStatusCode(), e.getMessage());
        return new ResponseEntity<>(errorDto, e.getStatusCode());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        log.info(e.getMessage());
        ErrorDto errorDto = new ErrorDto(HttpStatus.CONFLICT, e.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.CONFLICT);
    }

    @Getter
    private static class ErrorDto {

        private final HttpStatus status;
        private final String error;

        public ErrorDto(HttpStatus status, String error) {
            this.status = status;
            this.error = error;
        }
    }
}
