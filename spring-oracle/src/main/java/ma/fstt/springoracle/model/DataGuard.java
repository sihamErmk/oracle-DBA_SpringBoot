package ma.fstt.springoracle.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "data_guard")
public class DataGuard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String primaryDb;
    private String standbyDb;
    private String syncStatus;
    private String failoverMode;


}