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
@Table(name = "VPD_POLICIES")
public class VPDPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String policyName;

    @Column(nullable = false)
    private String tableName;

    @Column(nullable = false)
    private String functionName;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String policyFunction;

    @Column(nullable = false)
    private String statementTypes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private boolean active;
}