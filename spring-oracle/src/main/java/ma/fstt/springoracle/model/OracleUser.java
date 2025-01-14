package ma.fstt.springoracle.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class OracleUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;
    private String defaultTablespace;
    private String temporaryTablespace;
    private String quotaLimit;

    private boolean accountLocked;
    private LocalDateTime passwordExpiryDate;
    private LocalDateTime lastLoginDate;
    private int failedLoginAttempts;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
