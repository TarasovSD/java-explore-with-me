package ru.practicum.explorewithme.exceptions;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends RuntimeException {


    @ExceptionHandler(value = UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(final UserNotFoundException e) {
        log.info(e.getMessage());
        return new ResponseEntity<>("Пользователь не найден", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = UserNameAlreadyExistException.class)
    public ResponseEntity<ErrorDto> handleUserNameAlreadyExistException(final UserNameAlreadyExistException e) {
        log.info(e.getMessage());
        ErrorDto errorDto = new ErrorDto(HttpStatus.CONFLICT, e.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = CategoryNotFoundException.class)
    public ResponseEntity<String> handleCategoryNotFoundException(final CategoryNotFoundException e) {
        log.info(e.getMessage());
        return new ResponseEntity<>("Категория не найдена", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = EventNotFoundException.class)
    public ResponseEntity<String> handleEventNotFoundException(final EventNotFoundException e) {
        log.info(e.getMessage());
        return new ResponseEntity<>("Событие не найдено", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = RequestNotFoundException.class)
    public ResponseEntity<String> handleRequestNotFoundException(final RequestNotFoundException e) {
        log.info(e.getMessage());
        return new ResponseEntity<>("Запрос не найден", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = RequestNotConfirmedException.class)
    public ResponseEntity<String> handleRequestNotConfirmedException(final RequestNotConfirmedException e) {
        log.info(e.getMessage());
        return new ResponseEntity<>("Превышено число запросов на участие в событии", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = RequestNotCreatedException.class)
    public ResponseEntity<String> handleRequestNotCreatedException(final RequestNotCreatedException e) {
        log.info(e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = EventHasNoStatusPendingException.class)
    public ResponseEntity<String> handleEventHasNoStatusPendingException(final EventHasNoStatusPendingException e) {
        log.info(e.getMessage());
        return new ResponseEntity<>("Событие должно иметь статус Pending", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = CompilationNotFoundException.class)
    public ResponseEntity<String> handleCompilationNotFoundException(final CompilationNotFoundException e) {
        log.info(e.getMessage());
        return new ResponseEntity<>("Подборка не найдена", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleThrowable(final Throwable e) {
        log.info(e.getMessage());
        return new ResponseEntity<>("Произошла непредвиденная ошибка", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleNoSuchElementException(final NoSuchElementException e) {
        log.info(e.getMessage());
        return new ResponseEntity<>("Объект отсутствует в БД", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        log.info(e.getMessage());
        ErrorDto errorDto = new ErrorDto(HttpStatus.CONFLICT, getMessage());
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
