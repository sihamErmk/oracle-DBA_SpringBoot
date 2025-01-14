package ma.fstt.springoracle.exception;

public class PrivilegeNotFoundException extends RuntimeException {
  public PrivilegeNotFoundException(String message) {
    super(message);
  }
}

