package ir.darkdeveloper.anbarinoo.exception;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String msg) {
        super(msg);
    }

    public ForbiddenException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

}
