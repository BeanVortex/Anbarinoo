package ir.darkdeveloper.anbarinoo.exception.advice;

import java.time.LocalDateTime;

import ir.darkdeveloper.anbarinoo.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import ir.darkdeveloper.anbarinoo.model.ExceptionModel;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(PasswordException.class)
    public ResponseEntity<?> handleNonEqualPasswords(PasswordException e) {
        var em = new ExceptionModel(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST, LocalDateTime.now());
        return new ResponseEntity<>(em, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataExistsException.class)
    public ResponseEntity<?> handleUserExists(DataExistsException e) {
        var em = new ExceptionModel(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST, LocalDateTime.now());
        return new ResponseEntity<>(em, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<?> handleForbiddenRequests(ForbiddenException e) {
        var em = new ExceptionModel(e.getLocalizedMessage(), HttpStatus.FORBIDDEN, LocalDateTime.now());
        return new ResponseEntity<>(em, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequests(BadRequestException e) {
        var em = new ExceptionModel(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST, LocalDateTime.now());
        return new ResponseEntity<>(em, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<?> handleInternalErrors(InternalServerException e) {
        var em = new ExceptionModel(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR, LocalDateTime.now());
        return new ResponseEntity<>(em, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EmailNotValidException.class)
    public ResponseEntity<?> handleEmailValidation(EmailNotValidException e) {
        var em = new ExceptionModel(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST, LocalDateTime.now());
        return new ResponseEntity<>(em, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoContentException.class)
    public ResponseEntity<?> handleNoContent(NoContentException e) {
        var em = new ExceptionModel(e.getLocalizedMessage(), HttpStatus.NO_CONTENT, LocalDateTime.now());
        return new ResponseEntity<>(em, HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        var em = new ExceptionModel(e.getLocalizedMessage(), HttpStatus.NOT_ACCEPTABLE, LocalDateTime.now());
        return new ResponseEntity<>(em, HttpStatus.NOT_ACCEPTABLE);
    }


}
