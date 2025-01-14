package ma.fstt.springoracle.repository;

import ma.fstt.springoracle.model.TDEConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TDEConfigRepository extends JpaRepository<TDEConfig, Long> {
    TDEConfig findByTableNameAndColumnName(String tableName, String columnName);
    boolean existsByTableNameAndColumnName(String tableName, String columnName);
}