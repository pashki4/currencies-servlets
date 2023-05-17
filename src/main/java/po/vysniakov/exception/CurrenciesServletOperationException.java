package po.vysniakov.exception;

public class CurrenciesServletOperationException extends RuntimeException {
    public CurrenciesServletOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
