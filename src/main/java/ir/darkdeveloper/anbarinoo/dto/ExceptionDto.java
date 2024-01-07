package ir.darkdeveloper.anbarinoo.dto;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ExceptionDto(String message, HttpStatus httpStatus, LocalDateTime timestamp) {
}
