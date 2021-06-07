package ir.darkdeveloper.anbarinoo.model;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExceptionModel {
    private final String message;
    private final HttpStatus httpStatus;
    private final LocalDateTime timestamp;
}
