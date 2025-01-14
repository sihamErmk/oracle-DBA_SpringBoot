package ma.fstt.springoracle.repository;

import ma.fstt.springoracle.model.VPDPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VPDPolicyRepository extends JpaRepository<VPDPolicy, Long> {
    VPDPolicy findByPolicyName(String policyName);
    boolean existsByPolicyName(String policyName);
    VPDPolicy findByTableNameAndActive(String tableName, boolean active);
}