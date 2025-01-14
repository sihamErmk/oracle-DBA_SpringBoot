package ma.fstt.springoracle.repository;

import ma.fstt.springoracle.model.PerformanceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PerformanceMetricsRepository extends JpaRepository<PerformanceData, Long> {
    List<PerformanceData> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}