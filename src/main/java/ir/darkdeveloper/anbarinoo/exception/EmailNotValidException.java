package ir.darkdeveloper.anbarinoo.exception;

public class EmailNotValidException extends RuntimeException{
    
    public EmailNotValidException(String msg) {
        super(msg);
    }

    public EmailNotValidException(String message, Throwable cause) {
        super(message, cause);
    }

}
