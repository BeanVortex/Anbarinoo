package ir.darkdeveloper.anbarinoo.exception;

public class InternalServerException extends RuntimeException {
    public InternalServerException(String msg) {
        super(msg);
    }

    public InternalServerException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

}
