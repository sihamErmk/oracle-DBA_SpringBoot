package ma.fstt.springoracle.exception.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OracleSecurityException.class)
    public ResponseEntity<Object> handleOracleSecurityException(OracleSecurityException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        body.put("errorCode", ex.getErrorCode());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TDEConfigurationException.class)
    public ResponseEntity<Object> handleTDEException(TDEConfigurationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "TDE Configuration Error: " + ex.getMessage());
        body.put("errorCode", ex.getErrorCode());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AuditConfigurationException.class)
    public ResponseEntity<Object> handleAuditException(AuditConfigurationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "Audit Configuration Error: " + ex.getMessage());
        body.put("errorCode", ex.getErrorCode());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(VPDConfigurationException.class)
    public ResponseEntity<Object> handleVPDException(VPDConfigurationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "VPD Configuration Error: " + ex.getMessage());
        body.put("errorCode", ex.getErrorCode());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}