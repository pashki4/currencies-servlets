package po.vysniakov.exception;

import po.vysniakov.servlet.ExchangeRateServlet;

public class ExchangeRateServletOperationException extends RuntimeException{
    public ExchangeRateServletOperationException(String message, Throwable e) {
        super(message, e);
    }
}
