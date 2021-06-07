package ir.darkdeveloper.anbarinoo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DataExistsException extends BadRequestException {

    public DataExistsException(String msg) {
        super(msg);
    }

    public DataExistsException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

}
