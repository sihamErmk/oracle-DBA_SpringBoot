package ma.fstt.springoracle.service;

import lombok.RequiredArgsConstructor;
import ma.fstt.springoracle.exception.security.AuditConfigurationException;
import ma.fstt.springoracle.exception.security.TDEConfigurationException;
import ma.fstt.springoracle.exception.security.VPDConfigurationException;
import ma.fstt.springoracle.model.AuditConfig;
import ma.fstt.springoracle.model.TDEConfig;
import ma.fstt.springoracle.model.VPDPolicy;
import ma.fstt.springoracle.repository.AuditConfigRepository;
import ma.fstt.springoracle.repository.TDEConfigRepository;
import ma.fstt.springoracle.repository.VPDPolicyRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OracleSecurityServiceImpl implements OracleSecurityService {
    private final TDEConfigRepository tdeConfigRepository;
    private final VPDPolicyRepository vpdPolicyRepository;
    private final AuditConfigRepository auditConfigRepository;
    private final JdbcTemplate jdbcTemplate;

    // TDE Methods
    @Transactional
    public TDEConfig enableColumnEncryption(String tableName, String columnName, String algorithm, String username) {
        if (tdeConfigRepository.existsByTableNameAndColumnName(tableName, columnName)) {
            throw new TDEConfigurationException("Encryption already configured for this column");
        }

        try {
            // First check if wallet is open
            String checkWalletSql = "SELECT STATUS FROM V$ENCRYPTION_WALLET WHERE ROWNUM = 1";

            String walletStatus = jdbcTemplate.queryForObject(checkWalletSql, String.class);

            if (!"OPEN".equals(walletStatus)) {
                throw new TDEConfigurationException("Encryption wallet is not open. Please contact database administrator.");
            }

            String sql = String.format(
                    "ALTER TABLE %s MODIFY %s ENCRYPT USING '%s'",
                    tableName, columnName, algorithm
            );
            jdbcTemplate.execute(sql);

            TDEConfig config = new TDEConfig();
            config.setTableName(tableName);
            config.setColumnName(columnName);
            config.setEncryptionAlgorithm(algorithm);
            config.setCreatedAt(LocalDateTime.now());
            config.setCreatedBy(username);
            config.setActive(true);

            return tdeConfigRepository.save(config);
        } catch (Exception e) {
            throw new TDEConfigurationException("Failed to enable TDE encryption", e);
        }
    }

    @Transactional
    public void disableColumnEncryption(String tableName, String columnName) {
        TDEConfig config = tdeConfigRepository.findByTableNameAndColumnName(tableName, columnName);
        if (config == null) {
            throw new TDEConfigurationException("No TDE configuration found");
        }

        try {
            String sql = String.format("ALTER TABLE %s MODIFY %s DECRYPT", tableName, columnName);
            jdbcTemplate.execute(sql);

            config.setActive(false);
            tdeConfigRepository.save(config);
        } catch (Exception e) {
            throw new TDEConfigurationException("Failed to disable TDE encryption", e);
        }
    }

    public List<TDEConfig> getAllTDEConfigurations() {
        return tdeConfigRepository.findAll();
    }

    // VPD Methods
    @Transactional
    public VPDPolicy createPolicy(VPDPolicy policy, String username) {
        if (vpdPolicyRepository.existsByPolicyName(policy.getPolicyName())) {
            // Drop existing policy first
            try {
                dropPolicy(policy.getPolicyName());
            } catch (Exception e) {
                throw new VPDConfigurationException("Failed to drop existing policy", e);
            }
        }

        try {
            String createFunctionSql = String.format(
                    "CREATE OR REPLACE FUNCTION %s(schema_var IN VARCHAR2, table_var IN VARCHAR2) " +
                            "RETURN VARCHAR2 AS BEGIN %s END;",
                    policy.getFunctionName(),
                    policy.getPolicyFunction()
            );
            jdbcTemplate.execute(createFunctionSql);

            String addPolicySql = String.format(
                    "BEGIN DBMS_RLS.ADD_POLICY(" +
                            "object_schema => USER, " +
                            "object_name => '%s', " +
                            "policy_name => '%s', " +
                            "function_schema => USER, " +
                            "policy_function => '%s', " +
                            "statement_types => '%s'); END;",
                    policy.getTableName(),
                    policy.getPolicyName(),
                    policy.getFunctionName(),
                    policy.getStatementTypes()
            );
            jdbcTemplate.execute(addPolicySql);

            policy.setCreatedAt(LocalDateTime.now());
            policy.setCreatedBy(username);
            policy.setActive(true);

            return vpdPolicyRepository.save(policy);
        } catch (Exception e) {
            throw new VPDConfigurationException("Failed to create VPD policy", e);
        }
    }

    @Transactional
    public void dropPolicy(String policyName) {
        VPDPolicy policy = vpdPolicyRepository.findByPolicyName(policyName);
        if (policy == null) {
            throw new VPDConfigurationException("Policy not found");
        }

        try {
            String dropPolicySql = String.format(
                    "BEGIN DBMS_RLS.DROP_POLICY(" +
                            "object_schema => USER, " +
                            "object_name => '%s', " +
                            "policy_name => '%s'); END;",
                    policy.getTableName(),
                    policy.getPolicyName()
            );
            jdbcTemplate.execute(dropPolicySql);

            String dropFunctionSql = String.format("DROP FUNCTION %s", policy.getFunctionName());
            jdbcTemplate.execute(dropFunctionSql);

            policy.setActive(false);
            vpdPolicyRepository.save(policy);
        } catch (Exception e) {
            throw new VPDConfigurationException("Failed to drop VPD policy", e);
        }
    }

    public List<VPDPolicy> getAllPolicies() {
        return vpdPolicyRepository.findAll();
    }

    // Audit Methods
    @Transactional
    public AuditConfig enableAuditing(AuditConfig config, String username) {
        if (auditConfigRepository.existsByTableName(config.getTableName())) {
            throw new AuditConfigurationException("Audit already configured for this table");
        }

        try {
            StringBuilder auditSql = new StringBuilder("AUDIT POLICY emp_audit_policy");

            // Ensure you are using unified auditing instead of traditional auditing
            jdbcTemplate.execute(auditSql.toString());

            config.setCreatedAt(LocalDateTime.now());
            config.setCreatedBy(username);
            return auditConfigRepository.save(config);
        } catch (Exception e) {
            throw new AuditConfigurationException("Failed to enable auditing", e);
        }
    }


    @Transactional
    public void disableAuditing(String tableName) {
        AuditConfig config = auditConfigRepository.findByTableName(tableName);
        if (config == null) {
            throw new AuditConfigurationException("No audit configuration found for the specified table");
        }

        try {
            String sql = String.format("NOAUDIT ALL ON %s", tableName);
            jdbcTemplate.execute(sql);

            auditConfigRepository.delete(config);
        } catch (Exception e) {
            throw new AuditConfigurationException("Failed to disable auditing", e);
        }
    }

    public List<AuditConfig> getAllAuditConfigurations() {
        return auditConfigRepository.findAll();
    }
}