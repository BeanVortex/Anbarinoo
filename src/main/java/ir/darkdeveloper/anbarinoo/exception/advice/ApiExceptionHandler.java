package ir.darkdeveloper.anbarinoo.exception.advice;

import java.time.LocalDateTime;

import ir.darkdeveloper.anbarinoo.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import ir.darkdeveloper.anbarinoo.dto.ExceptionDto;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(PasswordException.class)
    public ResponseEntity<?> handleNonEqualPasswords(PasswordException e) {
        var ed = new ExceptionDto(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST, LocalDateTime.now());
        return new ResponseEntity<>(ed, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataExistsException.class)
    public ResponseEntity<?> handleUserExists(DataExistsException e) {
        var ed = new ExceptionDto(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST, LocalDateTime.now());
        return new ResponseEntity<>(ed, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<?> handleForbiddenRequests(ForbiddenException e) {
        var ed = new ExceptionDto(e.getLocalizedMessage(), HttpStatus.FORBIDDEN, LocalDateTime.now());
        return new ResponseEntity<>(ed, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequests(BadRequestException e) {
        var ed = new ExceptionDto(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST, LocalDateTime.now());
        return new ResponseEntity<>(ed, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<?> handleInternalErrors(InternalServerException e) {
        var ed = new ExceptionDto(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR, LocalDateTime.now());
        return new ResponseEntity<>(ed, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EmailNotValidException.class)
    public ResponseEntity<?> handleEmailValidation(EmailNotValidException e) {
        var ed = new ExceptionDto(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST, LocalDateTime.now());
        return new ResponseEntity<>(ed, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoContentException.class)
    public ResponseEntity<?> handleNoContent(NoContentException e) {
        var ed = new ExceptionDto(e.getLocalizedMessage(), HttpStatus.NO_CONTENT, LocalDateTime.now());
        return new ResponseEntity<>(ed, HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        var ed = new ExceptionDto(e.getLocalizedMessage(), HttpStatus.NOT_ACCEPTABLE, LocalDateTime.now());
        return new ResponseEntity<>(ed, HttpStatus.NOT_ACCEPTABLE);
    }


}
