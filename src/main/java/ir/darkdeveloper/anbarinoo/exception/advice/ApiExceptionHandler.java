package ir.darkdeveloper.anbarinoo.exception.advice;

import ir.darkdeveloper.anbarinoo.dto.ExceptionDto;
import ir.darkdeveloper.anbarinoo.exception.*;
import org.hibernate.exception.DataException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.UncheckedIOException;
import java.time.LocalDateTime;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({
            PasswordException.class,
            DataExistsException.class,
            BadRequestException.class,
            EmailNotValidException.class,
            DataIntegrityViolationException.class,
            DataException.class
    })
    public ResponseEntity<ExceptionDto> handleBadRequests(RuntimeException e, Object o) {
        var message = e.getLocalizedMessage();
        if (e instanceof DataIntegrityViolationException)
            message = "Entity exists!";
        var ed = new ExceptionDto(message, HttpStatus.BAD_REQUEST, LocalDateTime.now());
        return new ResponseEntity<>(ed, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ExceptionDto> handleForbiddenRequests(ForbiddenException e) {
        var ed = new ExceptionDto(e.getLocalizedMessage(), HttpStatus.FORBIDDEN, LocalDateTime.now());
        return new ResponseEntity<>(ed, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({
            InternalServerException.class,
            UncheckedIOException.class
    })
    public ResponseEntity<ExceptionDto> handleInternalErrors(RuntimeException e) {
        var ed = new ExceptionDto(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR, LocalDateTime.now());
        return new ResponseEntity<>(ed, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionDto> handleNoContent(NotFoundException e) {
        var ed = new ExceptionDto(e.getLocalizedMessage(), HttpStatus.NOT_FOUND, LocalDateTime.now());
        return new ResponseEntity<>(ed, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionDto> handleArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        var ed = new ExceptionDto(e.getLocalizedMessage(), HttpStatus.NOT_ACCEPTABLE, LocalDateTime.now());
        return new ResponseEntity<>(ed, HttpStatus.NOT_ACCEPTABLE);
    }


}
