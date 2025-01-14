package ma.fstt.springoracle.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "backup_history")
public class BackupHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // e.g., FULL or INCREMENTAL

    @Column(nullable = false)
    private String status; // e.g., SUCCESS, FAILURE

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 500)
    private String details; // Optional field for additional information

    public BackupHistory() {}

    public BackupHistory(String type, String status, LocalDateTime timestamp, String details) {
        this.type = type;
        this.status = status;
        this.timestamp = timestamp;
        this.details = details;
    }

    // Getters and setters omitted for brevity
}
