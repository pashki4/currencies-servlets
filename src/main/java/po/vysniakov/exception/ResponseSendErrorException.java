package po.vysniakov.exception;

public class ResponseSendErrorException extends RuntimeException{
    public ResponseSendErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
