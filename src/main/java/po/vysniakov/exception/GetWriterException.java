package po.vysniakov.exception;

public class GetWriterException extends RuntimeException {
    public GetWriterException(String message, Throwable cause) {
        super(message, cause);
    }
    public GetWriterException(String message) {
        super(message);
    }
}
