package fpt.project.NeoNHS.exception;

public class InvalidCheckinAttempt extends BadRequestException {
    public InvalidCheckinAttempt(String message) {
        super(message);
    }
}
