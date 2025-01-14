package ma.fstt.springoracle.exception.security;

public class TDEConfigurationException extends OracleSecurityException {
    public TDEConfigurationException(String message) {
        super(message, "TDE_001");
    }

    public TDEConfigurationException(String message, Throwable cause) {
        super(message + ": " + cause.getMessage(), "TDE_002");
    }
}
