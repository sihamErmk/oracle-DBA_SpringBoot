package ma.fstt.springoracle.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "performance_data")
public class PerformanceData {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "performance_data_seq")
    @SequenceGenerator(name = "performance_data_seq", sequenceName = "ISEQ$$_72163", allocationSize = 1)
    private Long id;

    private Long snapId;

    private String name;

    private Long value;



    private Double cpuUsagePercent;

    private Double memoryUsageMB;

    private Double bufferCacheHitRatio;

    private Double ioOperationsPerSecond;

    private Double pgaUsageMB;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;
}