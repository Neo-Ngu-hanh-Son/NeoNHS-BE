package fpt.project.NeoNHS.exception;

public class EmailException extends BadRequestException {
    public EmailException(String message) {
        super(message);
    }
}
