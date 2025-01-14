package ma.fstt.springoracle.repository;

import ma.fstt.springoracle.model.BackupHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BackupHistoryRepository extends JpaRepository<BackupHistory, Long> {
}
