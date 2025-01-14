package ma.fstt.springoracle.repository;

import ma.fstt.springoracle.model.AuditConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditConfigRepository extends JpaRepository<AuditConfig, Long> {
    AuditConfig findByTableName(String tableName);
    boolean existsByTableName(String tableName);
}