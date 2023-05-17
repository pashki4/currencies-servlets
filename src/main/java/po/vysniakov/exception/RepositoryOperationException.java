package po.vysniakov.exception;

public class RepositoryOperationException extends RuntimeException {
    public RepositoryOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryOperationException(String message) {
        super(message);
    }
}
