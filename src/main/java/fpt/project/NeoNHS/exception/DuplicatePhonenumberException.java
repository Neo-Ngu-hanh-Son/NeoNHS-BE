package fpt.project.NeoNHS.exception;

public class DuplicatePhonenumberException extends BadRequestException {
    public DuplicatePhonenumberException(String message) {
        super(message);
    }
}
