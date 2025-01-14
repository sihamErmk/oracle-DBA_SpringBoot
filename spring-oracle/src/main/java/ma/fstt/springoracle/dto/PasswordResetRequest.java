package ma.fstt.springoracle.dto;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String newPassword;
}