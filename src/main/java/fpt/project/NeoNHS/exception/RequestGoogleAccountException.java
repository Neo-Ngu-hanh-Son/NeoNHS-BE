package fpt.project.NeoNHS.exception;

public class RequestGoogleAccountException extends RuntimeException {
    public RequestGoogleAccountException(String message) {
        super(message);
    }

    public RequestGoogleAccountException() {
        super("Please use Google account to login");
    }
}
