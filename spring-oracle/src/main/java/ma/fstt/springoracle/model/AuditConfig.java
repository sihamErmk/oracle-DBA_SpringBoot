package ma.fstt.springoracle.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "AUDIT_CONFIGS")
public class AuditConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tableName;

    @Column(nullable = false)
    private String auditLevel; // ALL, INSERT, UPDATE, DELETE

    @Column(nullable = false)
    private boolean auditSuccessful;

    @Column(nullable = false)
    private boolean auditFailed;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String createdBy;
}