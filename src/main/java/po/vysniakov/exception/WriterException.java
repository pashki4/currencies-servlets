package po.vysniakov.exception;

public class WriterException extends RuntimeException {
    public WriterException(String message, Throwable cause) {
        super(message, cause);
    }
    public WriterException(String message) {
        super(message);
    }
}
