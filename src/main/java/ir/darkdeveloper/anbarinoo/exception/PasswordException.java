package ir.darkdeveloper.anbarinoo.exception;


public class PasswordException extends RuntimeException {

    public PasswordException(String msg) {
        super(msg);
    }

    public PasswordException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
