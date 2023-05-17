package po.vysniakov.exception;

import java.io.IOException;

public class CurrencyServletOperationException extends RuntimeException {
    public CurrencyServletOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
