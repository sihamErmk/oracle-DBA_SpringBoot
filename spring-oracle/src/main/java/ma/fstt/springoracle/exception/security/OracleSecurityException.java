package ma.fstt.springoracle.exception.security;

public class OracleSecurityException extends RuntimeException {
    private final String errorCode;

    public OracleSecurityException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}