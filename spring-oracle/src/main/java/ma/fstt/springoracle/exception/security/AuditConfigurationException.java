package ma.fstt.springoracle.exception.security;

public class AuditConfigurationException extends OracleSecurityException {
    public AuditConfigurationException(String message) {
        super(message, "AUDIT_001");
    }

    public AuditConfigurationException(String message, Throwable cause) {
        super(message + ": " + cause.getMessage(), "AUDIT_002");
    }
}