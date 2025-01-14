package ma.fstt.springoracle.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UserDTO {
    private String username;
    private String password;
    private String defaultTablespace;
    private String temporaryTablespace;
    private String quotaLimit;
    private Set<String> roles;
    private PasswordPolicy passwordPolicy;

    @Data
    public static class PasswordPolicy {
        private int expiryDays;
        private int minLength = 8;
        private boolean requireSpecialChar;
        private boolean requireNumber;
        private boolean requireUpperCase;
    }
}

