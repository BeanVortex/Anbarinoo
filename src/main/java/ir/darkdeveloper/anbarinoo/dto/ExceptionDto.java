package ir.darkdeveloper.anbarinoo.dto;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

public record ExceptionDto(String message, HttpStatus httpStatus,
                           LocalDateTime timestamp) {
}
