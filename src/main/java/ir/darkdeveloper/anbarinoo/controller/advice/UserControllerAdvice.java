package ir.darkdeveloper.anbarinoo.controller.advice;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import ir.darkdeveloper.anbarinoo.exception.DataExistsException;
import ir.darkdeveloper.anbarinoo.exception.PasswordException;
import ir.darkdeveloper.anbarinoo.model.ExceptionModel;

@ControllerAdvice
public class UserControllerAdvice {

    @ExceptionHandler(PasswordException.class)
    public ResponseEntity<?> handleNonEqualPasswords(PasswordException e) {
        var em = new ExceptionModel(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST, LocalDateTime.now());
        return new ResponseEntity<>(em, HttpStatus.OK);
    }

    @ExceptionHandler(DataExistsException.class)
    public ResponseEntity<?> handleUserExists(DataExistsException e) {
        var em = new ExceptionModel(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST, LocalDateTime.now());
        return new ResponseEntity<>(em, HttpStatus.OK);
    }

}
