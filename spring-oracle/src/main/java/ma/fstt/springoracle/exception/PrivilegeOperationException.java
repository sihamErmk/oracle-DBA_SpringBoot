package ma.fstt.springoracle.exception;

public class PrivilegeOperationException extends RuntimeException {
    public PrivilegeOperationException(String message) {
        super(message);
    }

    public PrivilegeOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}