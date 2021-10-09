package ir.darkdeveloper.anbarinoo.model;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

public record ExceptionModel(String message, HttpStatus httpStatus,
                             LocalDateTime timestamp) {
}
