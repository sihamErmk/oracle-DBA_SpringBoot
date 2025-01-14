package ma.fstt.springoracle.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Privilege {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;
}
