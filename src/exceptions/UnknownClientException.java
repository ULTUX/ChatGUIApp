package exceptions;

public class UnknownClientException extends Exception{
    public UnknownClientException(String message) {
        super(message);
    }
}
