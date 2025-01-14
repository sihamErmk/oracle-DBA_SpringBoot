package ma.fstt.springoracle.service;

import ma.fstt.springoracle.model.AuditConfig;
import ma.fstt.springoracle.model.TDEConfig;
import ma.fstt.springoracle.model.VPDPolicy;

import java.util.List;

public interface OracleSecurityService {

    // TDE Methods
    TDEConfig enableColumnEncryption(String tableName, String columnName, String algorithm, String username);

    void disableColumnEncryption(String tableName, String columnName);

    List<TDEConfig> getAllTDEConfigurations();

    // VPD Methods
    VPDPolicy createPolicy(VPDPolicy policy, String username);

    void dropPolicy(String policyName);

    List<VPDPolicy> getAllPolicies();

    // Audit Methods
    AuditConfig enableAuditing(AuditConfig config, String username);

    void disableAuditing(String tableName);

    List<AuditConfig> getAllAuditConfigurations();
}
