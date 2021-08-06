package ir.darkdeveloper.anbarinoo.exception;

public class NoContentException extends RuntimeException {

    public NoContentException(String msg) {
        super(msg);
    }

    public NoContentException(String message, Throwable cause) {
        super(message, cause);
    }

}
