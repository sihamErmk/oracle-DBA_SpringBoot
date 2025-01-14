package ma.fstt.springoracle.repository;

import ma.fstt.springoracle.model.DataGuard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataGuardRepository extends JpaRepository<DataGuard, Long> {
}
