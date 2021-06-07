package ir.darkdeveloper.anbarinoo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PasswordException extends RuntimeException {

    public PasswordException(String msg) {
        super(msg);
    }

    public PasswordException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
