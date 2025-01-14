package ma.fstt.springoracle.exception.security;

public class VPDConfigurationException extends OracleSecurityException {
    public VPDConfigurationException(String message) {
        super(message, "VPD_001");
    }

    public VPDConfigurationException(String message, Throwable cause) {
        super(message + ": " + cause.getMessage(), "VPD_002");
    }
}