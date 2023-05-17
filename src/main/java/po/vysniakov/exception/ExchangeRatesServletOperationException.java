package po.vysniakov.exception;

public class ExchangeRatesServletOperationException extends RuntimeException {
    public ExchangeRatesServletOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
