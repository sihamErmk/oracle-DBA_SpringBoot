package ma.fstt.springoracle.repository;

import ma.fstt.springoracle.model.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {
    Optional<Privilege> findByName(String name);
    boolean existsByName(String name);
}
